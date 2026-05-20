package com.airesume.screening.service.ai;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.entity.JobDescription;
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
              "skills": string (comma-separated),
              "experience": string (concise narrative),
              "education": string,
              "certifications": string,
              "linkedinUrl": string or empty,
              "aiSummary": string (3-5 sentences for HR)
            }
            Use empty string when unknown. Do not invent employers or degrees not present in the text.
            """;

    private static final String MATCH_SYSTEM = """
            You compare a candidate resume to a job description for enterprise recruiting.
            Respond ONLY with JSON:
            {
              "matchScore": number (0-100, one decimal max),
              "missingSkills": string[] (from JD requirements not evidenced in resume),
              "matchingSkills": string[] (skills evidenced in resume that align with JD),
              "fitmentSummary": string (2-4 sentences),
              "recommendation": string (hire/no-hire leaning with rationale)
            }
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
                .build();
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
        return ResumeExtractionResult.builder()
                .fullName(nameGuess)
                .email(email)
                .phone(phone)
                .skills("")
                .experience("")
                .education("")
                .certifications("")
                .linkedinUrl("")
                .aiSummary("Heuristic summary: basic contact signals extracted; configure AI for full analysis.")
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
        return ResumeMatchResult.builder()
                .matchScore(BigDecimal.valueOf(score))
                .missingSkills(missing)
                .matchingSkills(matching)
                .fitmentSummary("Heuristic match based on keyword overlap; configure AI for nuanced scoring.")
                .recommendation("Review manually before making hiring decisions.")
                .build();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}
