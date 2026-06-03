package com.hacker.boooks.repository;
import com.hacker.boooks.entity.BookCopyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookCopyRepository extends JpaRepository<BookCopyEntity, Integer> {
    List<BookCopyEntity> findByBookId(Integer bookId);
    List<BookCopyEntity> findByBookIdAndStatus(Integer bookId, BookCopyEntity.Status status);
}