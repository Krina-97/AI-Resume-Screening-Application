package com.airesume.screening.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses common resume sections from plain text when AI is unavailable.
 */
public final class ResumeHeuristicParser {

    private static final Pattern LINKEDIN = Pattern.compile(
            "(https?://(?:www\\.)?linkedin\\.com/in/[\\w%-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECTION_HEADER = Pattern.compile(
            "^(SUMMARY|PROFESSIONAL SUMMARY|PROFILE|OBJECTIVE|SKILLS|TECHNICAL SKILLS|"
                    + "EXPERIENCE|WORK HISTORY|WORK EXPERIENCE|EMPLOYMENT|EDUCATION|CERTIFICATIONS?)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private ResumeHeuristicParser() {
    }

    public static ParsedResume parse(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return ParsedResume.empty();
        }
        String text = rawText.replace("\r\n", "\n");
        String[] lines = text.split("\n");

        String summary = extractSection(lines, "SUMMARY", "PROFESSIONAL SUMMARY", "PROFILE", "OBJECTIVE");
        String skills = extractSection(lines, "SKILLS", "TECHNICAL SKILLS");
        String experience = extractSection(lines, "EXPERIENCE", "WORK HISTORY", "WORK EXPERIENCE", "EMPLOYMENT");
        String education = extractSection(lines, "EDUCATION");
        String certifications = extractSection(lines, "CERTIFICATIONS");

        if (!StringUtils.hasText(skills)) {
            skills = heuristicSkillsFromText(text);
        }
        if (!StringUtils.hasText(experience)) {
            experience = heuristicExperienceBlock(lines);
        }
        if (!StringUtils.hasText(summary)) {
            summary = buildFallbackSummary(experience, skills, education);
        }

        Matcher lm = LINKEDIN.matcher(text);
        String linkedin = lm.find() ? lm.group(1) : "";

        return new ParsedResume(
                summary.trim(),
                skills.trim(),
                experience.trim(),
                education.trim(),
                certifications.trim(),
                linkedin.trim(),
                deriveStrengths(skills, experience, summary)
        );
    }

    private static final String[] ALL_HEADERS = {
            "SUMMARY", "PROFESSIONAL SUMMARY", "PROFILE", "OBJECTIVE",
            "SKILLS", "TECHNICAL SKILLS", "EXPERIENCE", "WORK HISTORY", "WORK EXPERIENCE", "EMPLOYMENT",
            "EDUCATION", "CERTIFICATIONS"
    };

    private static String extractSection(String[] lines, String... headers) {
        int start = -1;
        int end = lines.length;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isSectionHeader(line, headers)) {
                start = i + 1;
                continue;
            }
            if (start >= 0 && isAnySectionHeader(line)) {
                end = i;
                break;
            }
        }
        if (start < 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(line);
        }
        return sb.toString().trim();
    }

    private static boolean isSectionHeader(String line, String... headers) {
        if (!StringUtils.hasText(line)) {
            return false;
        }
        String upper = line.trim().toUpperCase(Locale.ROOT);
        for (String h : headers) {
            if (upper.equals(h) || upper.startsWith(h + ":")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnySectionHeader(String line) {
        return isSectionHeader(line, ALL_HEADERS);
    }

    private static String heuristicExperienceBlock(String[] lines) {
        StringBuilder sb = new StringBuilder();
        boolean inExperience = false;
        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("experience") || lower.contains("work history") || lower.contains("employment")) {
                inExperience = true;
                if (SECTION_HEADER.matcher(line).matches()) {
                    continue;
                }
            }
            if (inExperience && SECTION_HEADER.matcher(line).matches()) {
                break;
            }
            if (inExperience || line.matches(".*[—\\-–].*\\d{4}.*") || lower.contains("engineer")
                    || lower.contains("developer") || lower.contains("manager")) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        return sb.toString().trim();
    }

    private static String heuristicSkillsFromText(String text) {
        String[] tokens = {
                "Java", "Spring Boot", "Spring", "Hibernate", "SQL", "MySQL", "PostgreSQL", "AWS", "Azure", "GCP",
                "Kubernetes", "Docker", "React", "TypeScript", "JavaScript", "Python", "Node.js", "Kafka",
                "Microservices", "REST", "GraphQL", "JUnit", "Maven", "Git", "Agile", "Leadership"
        };
        List<String> found = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (lower.contains(token.toLowerCase(Locale.ROOT))) {
                found.add(token);
            }
        }
        return String.join(", ", found);
    }

    private static String buildFallbackSummary(String experience, String skills, String education) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(experience)) {
            String firstLine = experience.lines().findFirst().orElse(experience);
            sb.append(firstLine);
        }
        if (StringUtils.hasText(skills)) {
            if (!sb.isEmpty()) {
                sb.append(" Core skills include ").append(truncate(skills, 120)).append('.');
            } else {
                sb.append("Skilled in ").append(truncate(skills, 160)).append('.');
            }
        }
        if (!StringUtils.hasText(sb) && StringUtils.hasText(education)) {
            sb.append(education.lines().findFirst().orElse(education));
        }
        return sb.toString().trim();
    }

    private static String deriveStrengths(String skills, String experience, String summary) {
        List<String> strengths = new ArrayList<>();
        String blob = (summary + " " + experience).toLowerCase(Locale.ROOT);
        if (blob.contains("led ") || blob.contains("mentor") || blob.contains("lead ")) {
            strengths.add("Technical leadership");
        }
        if (blob.contains("microservice") || blob.contains("scalable") || blob.contains("latency")) {
            strengths.add("Scalable system design");
        }
        if (blob.contains("improv") || blob.contains("%")) {
            strengths.add("Measurable delivery impact");
        }
        if (blob.contains("agile") || blob.contains("cross-functional")) {
            strengths.add("Agile collaboration");
        }
        if (StringUtils.hasText(skills)) {
            String[] parts = skills.split(",");
            for (int i = 0; i < Math.min(3, parts.length); i++) {
                String s = parts[i].trim();
                if (StringUtils.hasText(s)) {
                    strengths.add(s);
                }
            }
        }
        return String.join(", ", strengths);
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max).trim() + "…";
    }

    public record ParsedResume(
            String summary,
            String skills,
            String experience,
            String education,
            String certifications,
            String linkedinUrl,
            String strengths
    ) {
        static ParsedResume empty() {
            return new ParsedResume("", "", "", "", "", "", "");
        }
    }
}
