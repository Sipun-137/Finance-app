package com.finance.financeapplication.audit.specification;

import com.finance.financeapplication.audit.model.AuditLog;
import com.finance.financeapplication.audit.DTO.AuditLogFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditSpecification {

    public static Specification<AuditLog> filter(AuditLogFilterDTO filter){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), filter.getUserId())
                );
            }


            if (filter.getAction() != null && !filter.getAction().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("action"), filter.getAction())
                );
            }

            if (filter.getResource() != null && !filter.getResource().isEmpty()) {
                predicates.add(
                        cb.equal(root.get("resource"), filter.getResource())
                );
            }

            if (filter.getFromDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate())
                );
            }

            if (filter.getToDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate())
                );
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
