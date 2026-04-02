package com.finance.financeapplication.record.repo;


import com.finance.financeapplication.common.enums.RecordType;
import com.finance.financeapplication.record.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, String>, JpaSpecificationExecutor<Category> {

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    List<Category> findByType(RecordType type);
}
