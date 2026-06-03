package com.hacker.boooks.repository;
import com.hacker.boooks.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}