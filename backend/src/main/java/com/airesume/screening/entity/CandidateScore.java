package com.airesume.screening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_description_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "job_description_id", nullable = false)
    private Long jobDescriptionId;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal matchScore = BigDecimal.ZERO;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "matching_skills", columnDefinition = "TEXT")
    private String matchingSkills;

    @Column(name = "fitment_summary", columnDefinition = "TEXT")
    private String fitmentSummary;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "interview_pros", columnDefinition = "TEXT")
    private String interviewPros;

    @Column(name = "interview_cons", columnDefinition = "TEXT")
    private String interviewCons;

    @CreationTimestamp
    @Column(name = "scored_at", updatable = false)
    private LocalDateTime scoredAt;
}
