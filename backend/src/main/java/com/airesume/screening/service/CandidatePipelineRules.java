package com.airesume.screening.service;

import com.airesume.screening.entity.CandidateStatus;

import java.math.BigDecimal;

/**
 * Maps resume–job match percentage to pipeline status after AI evaluation.
 * <ul>
 *   <li>&lt; 40% → {@link CandidateStatus#REJECTED}</li>
 *   <li>40–60% → {@link CandidateStatus#NEW}</li>
 *   <li>&gt; 60% → {@link CandidateStatus#SHORTLISTED}</li>
 * </ul>
 */
public final class CandidatePipelineRules {

    public static final double REJECT_BELOW_PERCENT = 40.0;
    public static final double SHORTLIST_ABOVE_PERCENT = 60.0;

    private CandidatePipelineRules() {}

    public static CandidateStatus statusFromMatchPercent(BigDecimal matchScore) {
        if (matchScore == null) {
            return CandidateStatus.NEW;
        }
        double percent = matchScore.doubleValue();
        if (percent < REJECT_BELOW_PERCENT) {
            return CandidateStatus.REJECTED;
        }
        if (percent <= SHORTLIST_ABOVE_PERCENT) {
            return CandidateStatus.NEW;
        }
        return CandidateStatus.SHORTLISTED;
    }

    public static String statusRuleDescription() {
        return "Below 40%% → Rejected; 40–60%% → New; above 60%% → Shortlisted";
    }
}
