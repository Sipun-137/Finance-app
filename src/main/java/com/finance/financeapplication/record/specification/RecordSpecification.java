package com.finance.financeapplication.record.specification;


import com.finance.financeapplication.record.DTO.request.RecordFilterRequest;
import com.finance.financeapplication.record.model.FinancialRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RecordSpecification {

    public static Specification<FinancialRecord> filter(RecordFilterRequest filter,String userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();



            if(filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()){
                String likePattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("description")), likePattern),
                                cb.like(cb.lower(root.get("category").get("name")), likePattern)
                        )
                );
            }

            // Filter by userId (required)
            predicates.add(
                    cb.equal(root.get("user").get("id"), userId)
            );

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getCategoryId() != null && !filter.getCategoryId().isBlank()) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getFrom() != null) {
                // recordDate >= from
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), filter.getFrom()));
            }

            if (filter.getTo() != null) {
                // recordDate <= to
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), filter.getTo()));
            }

            if(filter.getSortDir() != null && filter.getSortBy() != null){
                if(filter.getSortDir().equalsIgnoreCase("asc")){
                    query.orderBy(cb.asc(root.get(filter.getSortBy())));
                } else if(filter.getSortDir().equalsIgnoreCase("desc")){
                    query.orderBy(cb.desc(root.get(filter.getSortBy())));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
