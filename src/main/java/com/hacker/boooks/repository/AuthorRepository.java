package com.hacker.boooks.repository;
import com.hacker.boooks.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Integer> {
    Optional<AuthorEntity> findByNameIgnoreCase(String name);
}