package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateOverviewDto;
import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateScore;
import com.airesume.screening.utils.TextFormatUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CandidateOverviewBuilder {

    private CandidateOverviewBuilder() {
    }

    public static CandidateOverviewDto build(Candidate candidate, CandidateScore latestScore, String jobTitle) {
        return buildInternal(candidate, latestScore, jobTitle);
    }

    public static CandidateOverviewDto build(Candidate candidate, CandidateScoreDto latestScore, String jobTitle) {
        return buildInternal(candidate, toEntity(latestScore), jobTitle);
    }

    public static String formatStoredOverview(Candidate candidate, CandidateScore latestScore, String jobTitle) {
        return formatStoredOverview(build(candidate, latestScore, jobTitle));
    }

    public static String formatStoredOverview(Candidate candidate, CandidateScoreDto latestScore, String jobTitle) {
        return formatStoredOverview(build(candidate, latestScore, jobTitle));
    }

    private static CandidateOverviewDto buildInternal(Candidate candidate, CandidateScore latestScore, String jobTitle) {
        String work = firstNonBlank(candidate.getExperience(), candidate.getEducation(), "");
        String skills = formatSkills(candidate.getSkills());
        String strengths = buildStrengths(candidate.getStrengths(), latestScore);
        String executiveSummary = buildExecutiveSummary(candidate, work, latestScore);
        String verdict = deriveVerdict(latestScore);
        String headline = buildInterviewHeadline(latestScore, verdict, jobTitle);
        List<String> pros = buildInterviewPros(latestScore, candidate);
        List<String> concerns = buildInterviewConcerns(latestScore);

        BigDecimal matchPercent = latestScore != null ? latestScore.getMatchScore() : null;

        return CandidateOverviewDto.builder()
                .executiveSummary(normalize(executiveSummary))
                .interviewVerdict(verdict)
                .interviewHeadline(normalize(headline))
                .interviewPros(pros.stream().map(CandidateOverviewBuilder::normalize).toList())
                .interviewConcerns(concerns.stream().map(CandidateOverviewBuilder::normalize).toList())
                .matchPercent(matchPercent)
                .jobTitle(jobTitle)
                .workExperience(normalize(work))
                .skills(normalize(skills))
                .strengths(normalize(strengths))
                .build();
    }

    private static String normalize(String value) {
        return TextFormatUtils.normalizeDisplayText(value);
    }

    private static String formatStoredOverview(CandidateOverviewDto dto) {
        StringBuilder sb = new StringBuilder();
        appendSection(sb, "Executive summary", dto.getExecutiveSummary());
        appendSection(sb, "Interview verdict", dto.getInterviewVerdict() + " — " + dto.getInterviewHeadline());
        if (dto.getInterviewPros() != null && !dto.getInterviewPros().isEmpty()) {
            appendSection(sb, "Reasons to interview", String.join("\n", dto.getInterviewPros()));
        }
        if (dto.getInterviewConcerns() != null && !dto.getInterviewConcerns().isEmpty()) {
            appendSection(sb, "Concerns", String.join("\n", dto.getInterviewConcerns()));
        }
        appendSection(sb, "Work experience", dto.getWorkExperience());
        appendSection(sb, "Skills", dto.getSkills());
        appendSection(sb, "Strengths", dto.getStrengths());
        return sb.toString().trim();
    }

    private static CandidateScore toEntity(CandidateScoreDto dto) {
        if (dto == null) {
            return null;
        }
        return CandidateScore.builder()
                .matchScore(dto.getMatchScore())
                .matchingSkills(dto.getMatchingSkills())
                .missingSkills(dto.getMissingSkills())
                .fitmentSummary(dto.getFitmentSummary())
                .recommendation(dto.getRecommendation())
                .interviewPros(dto.getInterviewPros() != null ? String.join("\n", dto.getInterviewPros()) : "")
                .interviewCons(dto.getInterviewCons() != null ? String.join("\n", dto.getInterviewCons()) : "")
                .build();
    }

    private static void appendSection(StringBuilder sb, String label, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append("\n\n");
        }
        sb.append(label).append(": ").append(value.trim());
    }

    private static String buildExecutiveSummary(Candidate candidate, String work, CandidateScore score) {
        String aiSummary = candidate.getAiSummary();
        String fromStored = extractLabeledSection(aiSummary, "Executive summary");
        if (!StringUtils.hasText(fromStored)) {
            fromStored = extractLabeledSection(aiSummary, "Summary");
        }
        if (StringUtils.hasText(fromStored) && !isGenericText(fromStored)) {
            return expandToNarrative(fromStored);
        }
        if (StringUtils.hasText(aiSummary) && !looksLikeMatchOnlySummary(aiSummary) && !isGenericText(aiSummary)) {
            return expandToNarrative(aiSummary);
        }
        if (score != null && StringUtils.hasText(score.getFitmentSummary()) && !isGenericText(score.getFitmentSummary())) {
            return expandToNarrative(score.getFitmentSummary());
        }
        return expandToNarrative(composeExpandedSummary(candidate, work));
    }

    private static String expandToNarrative(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String trimmed = text.trim();
        if (trimmed.length() >= 280) {
            return trimmed;
        }
        return trimmed;
    }

    private static String composeExpandedSummary(Candidate candidate, String work) {
        StringBuilder sb = new StringBuilder();
        String name = StringUtils.hasText(candidate.getFullName()) ? candidate.getFullName().trim() : "This candidate";
        sb.append(name);
        if (StringUtils.hasText(candidate.getExperience())) {
            String exp = candidate.getExperience().trim();
            String firstRole = exp.lines().findFirst().orElse("").trim();
            if (StringUtils.hasText(firstRole)) {
                sb.append(" brings experience as ").append(firstRole);
            }
            String impact = exp.lines()
                    .map(String::trim)
                    .filter(l -> l.startsWith("•") || l.startsWith("-") || l.contains("%"))
                    .findFirst()
                    .orElse("");
            if (StringUtils.hasText(impact)) {
                sb.append(", with highlights such as ").append(impact.replaceFirst("^[•\\-]\\s*", ""));
            }
            sb.append(". ");
        } else {
            sb.append(" has uploaded a resume for evaluation. ");
        }
        if (StringUtils.hasText(candidate.getSkills())) {
            sb.append("Core technical skills include ").append(truncate(candidate.getSkills(), 160)).append(". ");
        }
        if (StringUtils.hasText(candidate.getEducation())) {
            sb.append("Education: ").append(candidate.getEducation().lines().findFirst().orElse("").trim()).append(". ");
        }
        if (StringUtils.hasText(candidate.getCertifications())) {
            sb.append("Certifications noted: ").append(truncate(candidate.getCertifications(), 120)).append(". ");
        }
        sb.append("See the interview recommendation below for whether to proceed to the next hiring stage.");
        return sb.toString().trim();
    }

    private static String deriveVerdict(CandidateScore score) {
        if (score == null || !StringUtils.hasText(score.getRecommendation())) {
            return "HOLD";
        }
        String upper = score.getRecommendation().trim().toUpperCase(Locale.ROOT);
        if (upper.startsWith("INTERVIEW") || upper.contains("SHORTLIST") || upper.contains("HIRE")) {
            return "INTERVIEW";
        }
        if (upper.startsWith("DO_NOT") || upper.startsWith("DO NOT") || upper.contains("REJECT") || upper.contains("PASS")) {
            return "DO_NOT_INTERVIEW";
        }
        if (upper.startsWith("HOLD")) {
            return "HOLD";
        }
        if (score.getMatchScore() != null) {
            double pct = score.getMatchScore().doubleValue();
            if (pct >= 60) {
                return "INTERVIEW";
            }
            if (pct < 40) {
                return "DO_NOT_INTERVIEW";
            }
        }
        return "HOLD";
    }

    private static String buildInterviewHeadline(CandidateScore score, String verdict, String jobTitle) {
        if (score != null && StringUtils.hasText(score.getRecommendation())) {
            String rec = score.getRecommendation().trim();
            int colon = rec.indexOf(':');
            if (colon > 0 && colon < 80) {
                return rec.substring(colon + 1).trim();
            }
            if (rec.length() > 120) {
                return rec.substring(0, 120).trim() + "…";
            }
            return rec;
        }
        String role = StringUtils.hasText(jobTitle) ? "\"" + jobTitle + "\"" : "this role";
        return switch (verdict) {
            case "INTERVIEW" -> "Strong fit for " + role + "—recommend scheduling an interview.";
            case "DO_NOT_INTERVIEW" -> "Limited fit for " + role + "—not recommended for interview at this time.";
            default -> "Mixed signals for " + role + "—review pros and cons before deciding.";
        };
    }

    private static List<String> buildInterviewPros(CandidateScore score, Candidate candidate) {
        List<String> pros = new ArrayList<>();
        if (score != null && StringUtils.hasText(score.getInterviewPros())) {
            for (String line : score.getInterviewPros().split("\\r?\\n")) {
                if (StringUtils.hasText(line)) {
                    pros.add(line.trim());
                }
            }
        }
        if (pros.isEmpty() && score != null) {
            addCsvTokensToList(pros, score.getMatchingSkills());
            if (!pros.isEmpty()) {
                List<String> sentences = new ArrayList<>();
                for (String skill : pros) {
                    sentences.add("Resume demonstrates " + skill + " aligned with the job requirements.");
                }
                pros = sentences;
            }
        }
        if (pros.isEmpty() && StringUtils.hasText(candidate.getStrengths())) {
            addCsvTokensToList(pros, candidate.getStrengths());
        }
        if (pros.isEmpty()) {
            pros.add("Re-score with AI keys enabled for tailored interview reasons.");
        }
        return pros.size() > 5 ? pros.subList(0, 5) : pros;
    }

    private static List<String> buildInterviewConcerns(CandidateScore score) {
        List<String> concerns = new ArrayList<>();
        if (score != null && StringUtils.hasText(score.getInterviewCons())) {
            for (String line : score.getInterviewCons().split("\\r?\\n")) {
                if (StringUtils.hasText(line)) {
                    concerns.add(line.trim());
                }
            }
        }
        if (concerns.isEmpty() && score != null) {
            addCsvTokensToList(concerns, score.getMissingSkills());
            if (!concerns.isEmpty()) {
                List<String> sentences = new ArrayList<>();
                for (String gap : concerns) {
                    sentences.add("Gap or weak evidence for: " + gap + ".");
                }
                concerns = sentences;
            }
        }
        if (concerns.isEmpty() && score != null && score.getMatchScore() != null
                && score.getMatchScore().doubleValue() < 40) {
            concerns.add("Overall match score is below the typical shortlist threshold.");
        }
        return concerns.size() > 4 ? concerns.subList(0, 4) : concerns;
    }

    private static void addCsvTokensToList(List<String> target, String csv) {
        if (!StringUtils.hasText(csv)) {
            return;
        }
        for (String part : csv.split("[,;]")) {
            String token = part.trim();
            if (StringUtils.hasText(token)) {
                target.add(token);
            }
        }
    }

    private static boolean isGenericText(String text) {
        if (!StringUtils.hasText(text)) {
            return true;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return lower.contains("heuristic match")
                || lower.contains("heuristic summary")
                || lower.contains("configure ai")
                || lower.contains("configure gemini")
                || lower.contains("configure openai")
                || lower.contains("keyword overlap")
                || lower.contains("upload with ai keys");
    }

    private static boolean looksLikeMatchOnlySummary(String text) {
        String t = text.trim();
        return t.startsWith("Match:") || t.startsWith("Match ");
    }

    private static String extractLabeledSection(String text, String label) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String marker = label + ":";
        int idx = text.indexOf(marker);
        if (idx < 0) {
            return "";
        }
        String rest = text.substring(idx + marker.length()).trim();
        int next = rest.indexOf("\n\n");
        if (next > 0) {
            return rest.substring(0, next).trim();
        }
        return rest.trim();
    }

    private static String buildStrengths(String extractedStrengths, CandidateScore score) {
        Set<String> parts = new LinkedHashSet<>();
        addCsvTokens(parts, extractedStrengths);
        if (score != null) {
            addCsvTokens(parts, score.getMatchingSkills());
        }
        if (parts.isEmpty()) {
            return "";
        }
        return String.join(", ", parts);
    }

    private static String formatSkills(String skills) {
        if (!StringUtils.hasText(skills)) {
            return "";
        }
        return skills.trim();
    }

    private static void addCsvTokens(Set<String> target, String csv) {
        if (!StringUtils.hasText(csv)) {
            return;
        }
        for (String part : csv.split("[,;]")) {
            String token = part.trim();
            if (StringUtils.hasText(token) && token.length() < 80) {
                target.add(token);
            }
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return "";
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max).trim() + "…";
    }

    public static List<String> splitSkillTags(String skills) {
        if (!StringUtils.hasText(skills)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String part : skills.split("[,;]")) {
            String token = part.trim();
            if (StringUtils.hasText(token)) {
                out.add(token);
            }
        }
        return out;
    }
}
