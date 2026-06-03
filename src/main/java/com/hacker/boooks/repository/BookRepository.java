package com.hacker.boooks.repository;

import com.hacker.boooks.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Integer> {
    List<BookEntity> findByAuthor(String name);

    List<BookEntity> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String titleKeyword, String authorKeyword);
}