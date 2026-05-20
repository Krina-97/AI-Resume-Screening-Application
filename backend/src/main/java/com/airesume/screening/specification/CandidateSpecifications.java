package com.airesume.screening.specification;

import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CandidateSpecifications {

    private CandidateSpecifications() {
    }

    public static Specification<Candidate> filter(Long jobId, CandidateStatus status, String query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (jobId != null) {
                predicates.add(cb.equal(root.get("jobDescriptionId"), jobId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(query)) {
                String like = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("skills")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
