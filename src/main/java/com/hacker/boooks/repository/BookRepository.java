package com.hacker.boooks.repository;
import com.hacker.boooks.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Integer> {
    @Query("SELECT b FROM BookEntity b JOIN b.authors a WHERE LOWER(a.name) = LOWER(:name)")
    List<BookEntity> findByAuthor(@Param("name") String name);

    @Query("SELECT DISTINCT b FROM BookEntity b LEFT JOIN b.authors a WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :titleKeyword, '%')) OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorKeyword, '%'))")
    List<BookEntity> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(@Param("titleKeyword") String titleKeyword, @Param("authorKeyword") String authorKeyword);

    List<BookEntity> findByTitleIgnoreCase(String title);
}