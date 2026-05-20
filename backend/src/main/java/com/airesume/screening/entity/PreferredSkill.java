package com.airesume.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preferred_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Builder.Default
    private Boolean active = true;
}
