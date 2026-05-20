package com.airesume.screening.service.ai;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.utils.ResumeHeuristicParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AiEngineService {

    private static final Pattern EMAIL = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("(\\+?\\d[\\d\\s().-]{7,}\\d)");

    private static final String EXTRACT_SYSTEM = """
            You are an expert resume parser. Extract structured candidate data from the resume text.
            Respond ONLY with JSON using this shape:
            {
              "fullName": string,
              "email": string,
              "phone": string,
              "skills": string (comma-separated technical and professional skills from the resume),
              "strengths": string (comma-separated top 4-6 evidenced strengths: achievements, leadership, domain depth),
              "experience": string (3-6 sentences summarizing roles, tenure, industries, and impact from work history),
              "education": string,
              "certifications": string,
              "linkedinUrl": string or empty,
              "aiSummary": string (5-6 complete sentences for HR: career arc, years of experience, top roles, measurable impact, and role fit—cite specifics from the resume)
            }
            Use empty string when unknown. Do not invent employers, degrees, or skills not present in the text.
            """;

    private static final String MATCH_SYSTEM = """
            You are an expert technical recruiter. Compare the candidate resume to the job description.
            Score how well the resume meets required skills, experience level, education, and role fit.
            Be evidence-based: only credit skills and experience clearly supported in the resume text.
            Respond ONLY with JSON:
            {
              "matchScore": number (0-100, one decimal max, overall fit percentage),
              "missingSkills": string[] (required JD skills or experience not evidenced in resume),
              "matchingSkills": string[] (skills or experience in resume that align with JD),
              "fitmentSummary": string (5-6 complete sentences: summarize career background, relevant achievements, alignment with this job, and overall fit—be specific),
              "recommendation": string (one sentence verdict starting with exactly one of: INTERVIEW, HOLD, or DO_NOT_INTERVIEW, then brief rationale),
              "interviewPros": string[] (3-5 concrete reasons to interview this candidate for this role),
              "interviewCons": string[] (2-4 concrete risks, gaps, or reasons to skip or delay interview)
            }
            """;

    private static final String JOB_ASSIST_SYSTEM = """
            You are an expert HR job description writer for an enterprise hiring platform.
            Draft a complete, professional job posting for the given department using ONLY skill names from the provided catalogs.
            The description must be substantial (at least 6 paragraphs) and include these sections with clear headings:
            Job Summary, Key Responsibilities (5+ bullet points), Required Qualifications, Preferred Qualifications,
            What We Offer, How to Apply.
            Respond ONLY with JSON:
            {
              "title": string,
              "description": string (full multi-section posting as plain text with newlines),
              "requiredSkills": string[] (exact names from allowed required skills only),
              "preferredSkills": string[] (exact names from allowed preferred skills only),
              "assistantMessage": string (1-2 friendly sentences explaining what you selected)
            }
            Pick skills that fit the department. Do not invent skill names outside the catalogs.
            """;

    private final AppProperties appProperties;
    private final OpenAiResumeClient openAiResumeClient;
    private final GeminiResumeClient geminiResumeClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AiEngineService(AppProperties appProperties,
                           OpenAiResumeClient openAiResumeClient,
                           GeminiResumeClient geminiResumeClient,
                           ObjectMapper objectMapper,
                           RestTemplate restTemplate) {
        this.appProperties = appProperties;
        this.openAiResumeClient = openAiResumeClient;
        this.geminiResumeClient = geminiResumeClient;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    public ResumeExtractionResult extractFromResumeText(String rawText) {
        AiResumeClient client = resolveClient();
        if (client != null) {
            try {
                String json = client.completeJson(EXTRACT_SYSTEM, "Resume text:\n" + truncate(rawText, 12000));
                return mapExtraction(json);
            } catch (Exception ex) {
                log.warn("AI extraction failed, using heuristic fallback", ex);
            }
        } else {
            log.warn("No AI provider configured; using heuristic extraction");
        }
        return heuristicExtraction(rawText);
    }

    public ResumeMatchResult matchResumeToJob(String resumeText, JobDescription jd) {
        AiResumeClient client = resolveClient();
        String jdPayload = buildJobPayload(jd);
        if (client != null) {
            try {
                String json = client.completeJson(MATCH_SYSTEM,
                        "Job description:\n" + jdPayload + "\n\nResume text:\n" + truncate(resumeText, 12000));
                return mapMatch(json);
            } catch (Exception ex) {
                log.warn("AI matching failed, using heuristic fallback", ex);
            }
        }
        return heuristicMatch(resumeText, jd);
    }

    public JobDescriptionAssistResult generateJobDescription(String department,
                                                             String templateTitle,
                                                             String templateDescription,
                                                             String templateRequiredSkills,
                                                             String templatePreferredSkills,
                                                             List<String> requiredCatalog,
                                                             List<String> preferredCatalog,
                                                             String titleOverride,
                                                             String location,
                                                             String experience,
                                                             boolean regenerate,
                                                             int variant) {
        AiResumeClient client = resolveClient();
        if (client != null) {
            try {
                StringBuilder user = new StringBuilder();
                user.append("Department: ").append(department).append("\n");
                if (StringUtils.hasText(titleOverride)) {
                    user.append("Preferred title: ").append(titleOverride).append("\n");
                }
                if (StringUtils.hasText(location)) {
                    user.append("Location: ").append(location).append("\n");
                }
                if (StringUtils.hasText(experience)) {
                    user.append("Experience level: ").append(experience).append("\n");
                }
                if (regenerate) {
                    user.append("\nRegeneration request: variant ").append(variant)
                            .append(". Write a distinctly different version (new wording, reorder bullets, fresh tone) ")
                            .append("while keeping the same department and skill constraints.\n");
                }
                user.append("\nReference template title: ").append(templateTitle).append("\n");
                user.append("Reference template description:\n").append(templateDescription).append("\n");
                user.append("Reference required skills: ").append(templateRequiredSkills).append("\n");
                user.append("Reference preferred skills: ").append(templatePreferredSkills).append("\n");
                user.append("\nAllowed required skills (pick from this list only):\n")
                        .append(String.join(", ", requiredCatalog)).append("\n");
                user.append("\nAllowed preferred skills (pick from this list only):\n")
                        .append(String.join(", ", preferredCatalog));

                String json = client.completeJson(JOB_ASSIST_SYSTEM, user.toString());
                JobDescriptionAssistResult result = mapJobAssist(json, true);
                if (result.getDescription() != null && result.getDescription().length() >= 400) {
                    return result;
                }
                log.warn("AI job description too short, using expanded template builder");
            } catch (Exception ex) {
                log.warn("AI job description assist failed, using template fallback", ex);
            }
        }
        return templateJobAssist(templateTitle, templateRequiredSkills, templatePreferredSkills,
                titleOverride, department, location, experience, regenerate, variant);
    }

    public String chat(String systemPrompt, String userMessage) {
        if (openAiResumeClient.isAvailable()) {
            return openAiChat(systemPrompt, userMessage);
        }
        if (geminiResumeClient.isAvailable()) {
            return geminiResumeClient.generatePlain(systemPrompt, userMessage);
        }
        return "AI is not configured. Set OPENAI_API_KEY or enable Gemini with GEMINI_API_KEY.";
    }

    private String openAiChat(String system, String user) {
        try {
            String url = appProperties.getOpenai().getBaseUrl() + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(appProperties.getOpenai().getApiKey());
            var body = new java.util.LinkedHashMap<String, Object>();
            body.put("model", appProperties.getOpenai().getModel());
            body.put("messages", java.util.List.of(
                    java.util.Map.of("role", "system", "content", system),
                    java.util.Map.of("role", "user", "content", user)
            ));
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("choices").path(0).path("message").path("content").asText("No response");
        } catch (Exception e) {
            log.error("OpenAI chat failed", e);
            return "Unable to reach AI provider.";
        }
    }

    private AiResumeClient resolveClient() {
        if ("gemini".equalsIgnoreCase(appProperties.getAiProvider()) && geminiResumeClient.isAvailable()) {
            return geminiResumeClient;
        }
        if (openAiResumeClient.isAvailable()) {
            return openAiResumeClient;
        }
        if (geminiResumeClient.isAvailable()) {
            return geminiResumeClient;
        }
        return null;
    }

    private ResumeExtractionResult mapExtraction(String json) throws Exception {
        JsonNode n = objectMapper.readTree(json);
        return ResumeExtractionResult.builder()
                .fullName(text(n, "fullName"))
                .email(text(n, "email"))
                .phone(text(n, "phone"))
                .skills(text(n, "skills"))
                .strengths(text(n, "strengths"))
                .experience(text(n, "experience"))
                .education(text(n, "education"))
                .certifications(text(n, "certifications"))
                .linkedinUrl(text(n, "linkedinUrl"))
                .aiSummary(text(n, "aiSummary"))
                .build();
    }

    private ResumeMatchResult mapMatch(String json) throws Exception {
        JsonNode n = objectMapper.readTree(json);
        BigDecimal score = BigDecimal.valueOf(n.path("matchScore").asDouble(0)).setScale(2, RoundingMode.HALF_UP);
        if (score.compareTo(BigDecimal.valueOf(100)) > 0) {
            score = BigDecimal.valueOf(100);
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        }
        return ResumeMatchResult.builder()
                .matchScore(score)
                .missingSkills(readStringArray(n, "missingSkills"))
                .matchingSkills(readStringArray(n, "matchingSkills"))
                .fitmentSummary(text(n, "fitmentSummary"))
                .recommendation(text(n, "recommendation"))
                .interviewPros(readStringArray(n, "interviewPros"))
                .interviewCons(readStringArray(n, "interviewCons"))
                .build();
    }

    private JobDescriptionAssistResult mapJobAssist(String json, boolean aiGenerated) throws Exception {
        JsonNode n = objectMapper.readTree(json);
        return JobDescriptionAssistResult.builder()
                .title(text(n, "title"))
                .description(text(n, "description"))
                .requiredSkills(readStringArray(n, "requiredSkills"))
                .preferredSkills(readStringArray(n, "preferredSkills"))
                .assistantMessage(text(n, "assistantMessage"))
                .aiGenerated(aiGenerated)
                .build();
    }

    private JobDescriptionAssistResult templateJobAssist(String templateTitle,
                                                         String templateRequired,
                                                         String templatePreferred,
                                                         String titleOverride,
                                                         String department,
                                                         String location,
                                                         String experience,
                                                         boolean regenerate,
                                                         int variant) {
        String title = StringUtils.hasText(titleOverride) ? titleOverride : templateTitle;
        int effectiveVariant = regenerate ? variant : 0;
        String description = com.airesume.screening.service.DepartmentJobDescriptionBuilder.build(
                department, location, experience, effectiveVariant, templateRequired, templatePreferred);
        int variantLabel = Math.floorMod(effectiveVariant, 3) + 1;
        String message = regenerate
                ? "Generated alternate job description (version " + variantLabel + " of 3) for "
                + department + " with matching skills selected."
                : "Generated full job description for " + department + " with required and nice-to-have skills. "
                + "Click Regenerate for alternate wording.";
        return JobDescriptionAssistResult.builder()
                .title(title)
                .description(description)
                .requiredSkills(parseCsvSkills(templateRequired))
                .preferredSkills(parseCsvSkills(templatePreferred))
                .assistantMessage(message)
                .aiGenerated(false)
                .build();
    }

    private List<String> parseCsvSkills(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String part : csv.split(",")) {
            if (StringUtils.hasText(part)) {
                out.add(part.trim());
            }
        }
        return out;
    }

    private List<String> readStringArray(JsonNode n, String field) {
        List<String> out = new ArrayList<>();
        if (n.path(field).isArray()) {
            for (JsonNode item : n.path(field)) {
                if (item.isTextual()) {
                    out.add(item.asText());
                }
            }
        }
        return out;
    }

    private String text(JsonNode n, String field) {
        return n.path(field).asText("");
    }

    private String buildJobPayload(JobDescription jd) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(jd.getTitle()).append("\n");
        if (StringUtils.hasText(jd.getDepartment())) {
            sb.append("Department: ").append(jd.getDepartment()).append("\n");
        }
        if (StringUtils.hasText(jd.getLocation())) {
            sb.append("Location: ").append(jd.getLocation()).append("\n");
        }
        if (StringUtils.hasText(jd.getExperienceRequired())) {
            sb.append("Experience: ").append(jd.getExperienceRequired()).append("\n");
        }
        if (StringUtils.hasText(jd.getRequiredSkills())) {
            sb.append("Required skills: ").append(jd.getRequiredSkills()).append("\n");
        }
        if (StringUtils.hasText(jd.getPreferredSkills())) {
            sb.append("Preferred skills: ").append(jd.getPreferredSkills()).append("\n");
        }
        sb.append("Description:\n").append(jd.getDescription());
        return sb.toString();
    }

    private ResumeExtractionResult heuristicExtraction(String raw) {
        String text = raw == null ? "" : raw;
        Matcher em = EMAIL.matcher(text);
        String email = em.find() ? em.group() : "";
        Matcher pm = PHONE.matcher(text);
        String phone = pm.find() ? pm.group(1).trim() : "";
        String[] lines = text.split("\\r?\\n");
        String nameGuess = "";
        for (String line : lines) {
            if (line != null && line.trim().length() > 3 && line.trim().length() < 80 && !line.contains("@")) {
                nameGuess = line.trim();
                break;
            }
        }
        ResumeHeuristicParser.ParsedResume parsed = ResumeHeuristicParser.parse(text);
        String summary = StringUtils.hasText(parsed.summary())
                ? parsed.summary()
                : "Experienced professional with skills and background detailed in the uploaded resume.";
        return ResumeExtractionResult.builder()
                .fullName(nameGuess)
                .email(email)
                .phone(phone)
                .skills(parsed.skills())
                .strengths(parsed.strengths())
                .experience(parsed.experience())
                .education(parsed.education())
                .certifications(parsed.certifications())
                .linkedinUrl(parsed.linkedinUrl())
                .aiSummary(summary)
                .build();
    }

    private ResumeMatchResult heuristicMatch(String resumeText, JobDescription jd) {
        String blob = (resumeText + " " + jd.getDescription() + " " + jd.getRequiredSkills()).toLowerCase(Locale.ROOT);
        String[] tokens = {"java", "spring", "sql", "aws", "kubernetes", "docker", "react", "python", "leadership"};
        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        String lowerResume = resumeText == null ? "" : resumeText.toLowerCase(Locale.ROOT);
        for (String t : tokens) {
            if (blob.contains(t)) {
                if (lowerResume.contains(t)) {
                    matching.add(t);
                } else if (jd.getDescription() != null && jd.getDescription().toLowerCase(Locale.ROOT).contains(t)) {
                    missing.add(t);
                }
            }
        }
        int score = Math.min(100, matching.size() * 15 + 20);
        String jobTitle = jd.getTitle() != null ? jd.getTitle() : "the role";
        String fitment = matching.isEmpty()
                ? String.format("Limited keyword overlap with \"%s\"; manual review recommended.", jobTitle)
                : String.format("Resume shows %d aligned skill(s) for \"%s\" (%s).",
                matching.size(), jobTitle, String.join(", ", matching));
        String verdict = score >= 60 ? "INTERVIEW" : (score >= 40 ? "HOLD" : "DO_NOT_INTERVIEW");
        String recommendation = verdict + ": " + (score >= 60
                ? "Strong skill alignment—schedule a technical interview to validate depth and culture fit."
                : score >= 40
                ? "Partial fit—consider a screening call before committing to a full loop."
                : "Weak alignment for this role—deprioritize unless hiring bar is flexible.");
        List<String> pros = new ArrayList<>();
        for (String skill : matching) {
            pros.add("Resume demonstrates " + skill + " experience relevant to the posting.");
            if (pros.size() >= 4) {
                break;
            }
        }
        if (pros.isEmpty()) {
            pros.add("Upload with AI keys enabled for detailed interview pros.");
        }
        List<String> cons = new ArrayList<>();
        for (String skill : missing) {
            cons.add("Limited evidence of " + skill + " in the resume for this role.");
            if (cons.size() >= 3) {
                break;
            }
        }
        if (cons.isEmpty() && score < 60) {
            cons.add("Overall match score is below the shortlist threshold for this job.");
        }
        String expandedFitment = buildHeuristicFitmentSummary(resumeText, jd, matching, missing, score);
        return ResumeMatchResult.builder()
                .matchScore(BigDecimal.valueOf(score))
                .missingSkills(missing)
                .matchingSkills(matching)
                .fitmentSummary(expandedFitment)
                .recommendation(recommendation)
                .interviewPros(pros)
                .interviewCons(cons)
                .build();
    }

    private static String buildHeuristicFitmentSummary(String resumeText, JobDescription jd,
                                                       List<String> matching, List<String> missing, int score) {
        String title = jd.getTitle() != null ? jd.getTitle() : "the open role";
        StringBuilder sb = new StringBuilder();
        sb.append("This candidate was evaluated against \"").append(title).append("\" using resume keyword analysis. ");
        sb.append("The profile shows a ").append(score).append("% alignment with the job requirements. ");
        if (!matching.isEmpty()) {
            sb.append("Notable overlaps include ").append(String.join(", ", matching)).append(". ");
        }
        if (!missing.isEmpty()) {
            sb.append("Areas needing validation include ").append(String.join(", ", missing)).append(". ");
        }
        sb.append("Configure GEMINI_API_KEY or OPENAI_API_KEY for a deeper narrative summary from the full resume text. ");
        sb.append("Use the interview recommendation below for a hiring decision on next steps.");
        return sb.toString().trim();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }

}
