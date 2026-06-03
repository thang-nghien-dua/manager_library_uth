package com.hacker.boooks.repository;

import com.hacker.boooks.entity.BookEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    private static final int id = 1;
    private static final String author = "Marcel Proust";

    @Autowired
    @SuppressWarnings("unused")
    private BookRepository underTest;

    @BeforeEach
    void setUp() {
        String title = "In Search of Lost Time";
        String genre = "Romance";
        Date publication = Date.valueOf("2004-03-01");
        boolean available = true;
        int holder = -1;

        BookEntity bookEntity = new BookEntity(id, title, author, genre, publication, available, holder);
        underTest.save(bookEntity);
    }

    @Test
    void findAll_WhenBooksExist_ShouldReturnListOfBooks() {
        // Act
        List<BookEntity> response = underTest.findAll();

        // Assert
        assertThat(response).hasSize(1);
    }

    @Test
    void findAll_WhenNoBooksExist_ShouldReturnEmptyList() {
        // Arrange
        underTest.deleteAll();

        // Act
        List<BookEntity> response = underTest.findAll();

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void findById_WhenBookExists_ShouldReturnBookById() {
        // Act
        Optional<BookEntity> response = underTest.findById(id);

        // Assert
        assertThat(response).isNotEmpty();
    }

    @Test
    void findById_WhenBookDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        int nonExistingId = -1;

        // Act
        Optional<BookEntity> response = underTest.findById(nonExistingId);

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void save_WhenBookIsNew_ShouldSaveBook() {
        // Arrange
        BookEntity book = new BookEntity();
        book.setBookId(2);
        book.setTitle("Sample Book");
        book.setAuthor("John Doe");
        book.setGenre("Fiction");
        book.setPublication(Date.valueOf("2004-03-01"));
        book.setIsAvailable(true);
        book.setHolder(null);

        // Act
        BookEntity savedBook = underTest.save(book);

        // Assert
        assertThat(savedBook.getBookId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getGenre()).isEqualTo(book.getGenre());
        assertThat(savedBook.getPublication()).isEqualTo(book.getPublication());
        assertThat(savedBook.getIsAvailable()).isEqualTo(book.getIsAvailable());
        assertThat(savedBook.getHolder()).isEqualTo(book.getHolder());
    }

    @Test
    void save_WhenBookAlreadyExists_ShouldUpdateBook() {
        // Arrange
        BookEntity existingBook = new BookEntity();
        existingBook.setBookId(2);
        existingBook.setTitle("Existing Book");
        existingBook.setAuthor("Jane Smith");
        existingBook.setGenre("Thriller");
        existingBook.setPublication(Date.valueOf("2004-03-01"));
        existingBook.setIsAvailable(true);
        existingBook.setHolder(null);
        underTest.save(existingBook);

        existingBook.setTitle("Updated Book");
        existingBook.setAuthor("John Doe");

        // Act
        BookEntity updatedBook = underTest.save(existingBook);

        // Assert
        assertThat(updatedBook.getBookId()).isEqualTo(existingBook.getBookId());
        assertThat(updatedBook.getTitle()).isEqualTo(existingBook.getTitle());
        assertThat(updatedBook.getAuthor()).isEqualTo(existingBook.getAuthor());
        assertThat(updatedBook.getGenre()).isEqualTo(existingBook.getGenre());
    }

    @Test
    void findByAuthor_WhenBookExists_ShouldReturnBookById() {
        // Act
        List<BookEntity> response = underTest.findByAuthor(author);

        // Assert
        assertThat(response).hasSize(1);
    }

    @Test
    void findByAuthor_WhenBookDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        String unknownAuthor = "unknown";

        // Act
        List<BookEntity> response = underTest.findByAuthor(unknownAuthor);

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase_WhenMatchExists_ShouldReturnMatchingBooks() {
        // Arrange
        String titleKeyword = "search";
        String authorKeyword = "proust";

        // Act
        List<BookEntity> results = underTest.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(titleKeyword, authorKeyword);

        // Assert
        assertThat(results).hasSize(1);
    }

    @Test
    void findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase_WhenNoMatchExists_ShouldReturnEmptyList() {
        // Arrange
        String titleKeyword = "nonexistent";
        String authorKeyword = "unknown";

        // Act
        List<BookEntity> results = underTest.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(titleKeyword, authorKeyword);

        // Assert
        assertThat(results).isEmpty();
    }


}