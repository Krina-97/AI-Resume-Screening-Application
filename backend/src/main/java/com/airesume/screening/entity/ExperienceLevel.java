package com.airesume.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experience_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    private Boolean active = true;
}
