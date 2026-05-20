package com.airesume.screening.config;

import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateScore;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.utils.ResumeHeuristicParser;
import org.springframework.util.StringUtils;
import com.airesume.screening.service.CandidateOverviewBuilder;
import com.airesume.screening.service.CandidateScoreService;
import com.airesume.screening.utils.TextFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Optional;

/**
 * Normalize stored names and backfill match scores when missing.
 * Pipeline status is always set by HR (defaults to NEW on upload).
 */
@Configuration
public class CandidatePipelineStartupSync {

    private static final Logger log = LoggerFactory.getLogger(CandidatePipelineStartupSync.class);

    @Bean
    @Order(2)
    public CommandLineRunner syncCandidatePipeline(CandidateRepository candidateRepository,
                                                   CandidateScoreRepository candidateScoreRepository,
                                                   JobDescriptionRepository jobDescriptionRepository,
                                                   ResumeRepository resumeRepository,
                                                   CandidateScoreService candidateScoreService) {
        return args -> {
            long total = candidateRepository.count();
            log.info("Persistent store loaded: {} candidate(s) in database", total);

            var activeJobs = jobDescriptionRepository.findByActiveTrueOrderByCreatedAtDesc();
            Long fallbackJobId = activeJobs.isEmpty() ? null : activeJobs.get(0).getId();

            for (Candidate c : candidateRepository.findAll()) {
                boolean dirty = false;

                if (c.getFullName() != null && !c.getFullName().isBlank()) {
                    String titled = TextFormatUtils.toTitleCaseName(c.getFullName());
                    if (!titled.equals(c.getFullName())) {
                        c.setFullName(titled);
                        dirty = true;
                    }
                }

                if (enrichFromResumeText(c, resumeRepository)) {
                    dirty = true;
                }
                if (normalizeCandidateTextFields(c)) {
                    dirty = true;
                }

                Optional<CandidateScore> latest = candidateScoreRepository.findFirstByCandidateIdOrderByScoredAtDesc(c.getId());

                if (latest.isEmpty() && fallbackJobId != null) {
                    Long jobId = c.getJobDescriptionId() != null ? c.getJobDescriptionId() : fallbackJobId;
                    try {
                        candidateScoreService.scoreCandidate(c.getId(), jobId, null);
                        if (c.getJobDescriptionId() == null) {
                            c.setJobDescriptionId(jobId);
                            dirty = true;
                        }
                        latest = candidateScoreRepository.findFirstByCandidateIdOrderByScoredAtDesc(c.getId());
                    } catch (Exception e) {
                        log.warn("Startup sync: could not score candidate {}: {}", c.getId(), e.getMessage());
                    }
                }

                String jobTitle = null;
                Long overviewJobId = c.getJobDescriptionId() != null
                        ? c.getJobDescriptionId()
                        : latest.map(CandidateScore::getJobDescriptionId).orElse(fallbackJobId);
                if (overviewJobId != null) {
                    jobTitle = jobDescriptionRepository.findById(overviewJobId)
                            .map(j -> j.getTitle())
                            .orElse(null);
                }
                String refreshedOverview = CandidateOverviewBuilder.formatStoredOverview(c, latest.orElse(null), jobTitle);
                if (refreshedOverview != null
                        && (!refreshedOverview.equals(c.getAiSummary()) || isGenericStoredOverview(c.getAiSummary()))) {
                    c.setAiSummary(refreshedOverview);
                    dirty = true;
                }

                if (dirty) {
                    candidateRepository.save(c);
                }
            }
        };
    }

    private static boolean normalizeCandidateTextFields(Candidate c) {
        boolean dirty = false;
        String experience = TextFormatUtils.normalizeDisplayText(c.getExperience());
        if (experience != null && !experience.equals(c.getExperience())) {
            c.setExperience(experience);
            dirty = true;
        }
        String skills = TextFormatUtils.normalizeDisplayText(c.getSkills());
        if (skills != null && !skills.equals(c.getSkills())) {
            c.setSkills(skills);
            dirty = true;
        }
        String strengths = TextFormatUtils.normalizeDisplayText(c.getStrengths());
        if (strengths != null && !strengths.equals(c.getStrengths())) {
            c.setStrengths(strengths);
            dirty = true;
        }
        String aiSummary = TextFormatUtils.normalizeDisplayText(c.getAiSummary());
        if (aiSummary != null && !aiSummary.equals(c.getAiSummary())) {
            c.setAiSummary(aiSummary);
            dirty = true;
        }
        return dirty;
    }

    private static boolean isGenericStoredOverview(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String lower = text.toLowerCase();
        return lower.contains("heuristic match")
                || lower.contains("heuristic summary")
                || lower.contains("keyword overlap")
                || lower.contains("configure ai");
    }

    private static boolean enrichFromResumeText(Candidate c, ResumeRepository resumeRepository) {
        var resumeOpt = resumeRepository.findById(c.getResumeId());
        if (resumeOpt.isEmpty() || !StringUtils.hasText(resumeOpt.get().getRawText())) {
            return false;
        }
        ResumeHeuristicParser.ParsedResume parsed = ResumeHeuristicParser.parse(resumeOpt.get().getRawText());
        boolean dirty = false;
        if (!StringUtils.hasText(c.getSkills()) && StringUtils.hasText(parsed.skills())) {
            c.setSkills(parsed.skills());
            dirty = true;
        }
        if (!StringUtils.hasText(c.getExperience()) && StringUtils.hasText(parsed.experience())) {
            c.setExperience(parsed.experience());
            dirty = true;
        }
        if (!StringUtils.hasText(c.getStrengths()) && StringUtils.hasText(parsed.strengths())) {
            c.setStrengths(parsed.strengths());
            dirty = true;
        }
        if (!StringUtils.hasText(c.getEducation()) && StringUtils.hasText(parsed.education())) {
            c.setEducation(parsed.education());
            dirty = true;
        }
        if (!StringUtils.hasText(c.getCertifications()) && StringUtils.hasText(parsed.certifications())) {
            c.setCertifications(parsed.certifications());
            dirty = true;
        }
        if (!StringUtils.hasText(c.getLinkedinUrl()) && StringUtils.hasText(parsed.linkedinUrl())) {
            c.setLinkedinUrl(parsed.linkedinUrl());
            dirty = true;
        }
        return dirty;
    }
}
