package com.airesume.screening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "linkedin_verification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedInVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "search_query")
    private String searchQuery;

    @Column(name = "profile_headline")
    private String profileHeadline;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "screenshot_path", length = 1000)
    private String screenshotPath;

    @Column(name = "log_details", columnDefinition = "TEXT")
    private String logDetails;

    @CreationTimestamp
    @Column(name = "verified_at", updatable = false)
    private LocalDateTime verifiedAt;
}
