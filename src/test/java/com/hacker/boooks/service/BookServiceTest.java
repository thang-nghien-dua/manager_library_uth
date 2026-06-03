package com.hacker.boooks.service;

import com.hacker.boooks.bean.AuthorProfile;
import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.BookBO;
import com.hacker.boooks.bean.BookProfile;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private LogRepository logRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BookServiceImpl underTest;

    @Test
    void getBooks_WhenBooksExist_ShouldReturnListOfBooks() {
        // Arrange
        List<BookEntity> bookEntities = List.of(
                new BookEntity(1, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), true, null),
                new BookEntity(2, "Book 2", "Author 1", "Genre 2", Date.valueOf(LocalDate.now()), false, 1)
        );

        when(bookRepository.findAll()).thenReturn(bookEntities);

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getBooks_WhenNoBooksExist_ShouldReturnEmptyList() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getBooks_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getBook_WhenBookExists_ShouldReturnBook() {
        // Arrange
        int bookId = 1;
        BookEntity bookEntity = new BookEntity(1, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), true, null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));

        // Act
        ResponseEntity<Book> response = underTest.getBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookId()).isEqualTo(bookEntity.getBookId());
        assertThat(response.getBody().getTitle()).isEqualTo(bookEntity.getTitle());
        assertThat(response.getBody().getAuthor()).isEqualTo(bookEntity.getAuthor());
    }

    @Test
    void getBook_WhenBookDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Book> response = underTest.getBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getBook_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Book> response = underTest.getBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getBookProfile_WhenBookExists_ShouldReturnBookProfile() {
        // Arrange
        int bookId = 1;
        BookEntity bookEntity = new BookEntity(1, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), true, null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));

        // Act
        ResponseEntity<BookProfile> response = underTest.getBookProfile(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo(bookEntity.getTitle());
        assertThat(response.getBody().getAuthor()).isEqualTo(bookEntity.getAuthor());
        assertThat(response.getBody().getGenre()).isEqualTo(bookEntity.getGenre());
        assertThat(response.getBody().isAvailable()).isEqualTo(bookEntity.getIsAvailable());
    }

    @Test
    void getBookProfile_WhenBookDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<BookProfile> response = underTest.getBookProfile(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getBookProfile_WhenBookIsNotAvailable_ShouldSetHolderInBookProfile() {
        // Arrange
        int bookId = 1;
        int memberId = 1;
        BookEntity bookEntity = new BookEntity(bookId, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), false, memberId);
        MemberEntity memberEntity = new MemberEntity(memberId, "John Doe", "john@example.com", "1234567890");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberEntity));

        // Act
        ResponseEntity<BookProfile> response = underTest.getBookProfile(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHolder()).isNotNull();
        assertThat(response.getBody().getHolder().getMemberId()).isEqualTo(memberEntity.getMemberId());
        assertThat(response.getBody().getHolder().getName()).isEqualTo(memberEntity.getName());
        assertThat(response.getBody().getHolder().getEmail()).isEqualTo(memberEntity.getEmail());
        assertThat(response.getBody().getHolder().getPhoneNumber()).isEqualTo(memberEntity.getPhoneNumber());
    }

    @Test
    void getBookProfile_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<BookProfile> response = underTest.getBookProfile(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void addBook_WhenValidBookBO_ShouldReturnSuccessMessage() {
        // Arrange
        BookBO bookBO = new BookBO("Book 1", "Author 1", "Genre 1", LocalDate.now());

        // Act
        ResponseEntity<String> response = underTest.addBook(bookBO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Book added successfully");
        verify(bookRepository).save(any(BookEntity.class));
    }

    @Test
    void addBook_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        BookBO bookBO = new BookBO("Book 1", "Author 1", "Genre 1", LocalDate.now());
        when(bookRepository.save(any(BookEntity.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<String> response = underTest.addBook(bookBO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(bookRepository).save(any(BookEntity.class));
    }

    @Test
    void updateBook_WhenBookExists_ShouldReturnSuccessMessage() {
        // Arrange
        int bookId = 1;
        BookBO bookBO = new BookBO("Updated Book", "Updated Author", "Updated Genre", LocalDate.now());
        BookEntity existingBook = new BookEntity();
        existingBook.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // Act
        ResponseEntity<String> response = underTest.updateBook(bookId, bookBO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Book updated successfully");
        assertThat(existingBook.getTitle()).isEqualTo(bookBO.getTitle());
        assertThat(existingBook.getAuthor()).isEqualTo(bookBO.getAuthor());
        assertThat(existingBook.getGenre()).isEqualTo(bookBO.getGenre());
        assertThat(existingBook.getPublication()).isEqualTo(Date.valueOf(bookBO.getPublicationDate()));
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBook_WhenBookDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        int bookId = 1;
        BookBO bookBO = new BookBO("Updated Book", "Updated Author", "Updated Genre", LocalDate.now());
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.updateBook(bookId, bookBO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(bookRepository, never()).save(any(BookEntity.class));
    }

    @Test
    void updateBook_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        int bookId = 1;
        BookBO bookBO = new BookBO("Updated Book", "Updated Author", "Updated Genre", LocalDate.now());
        BookEntity existingBook = new BookEntity();
        existingBook.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<String> response = underTest.updateBook(bookId, bookBO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(bookRepository).save(existingBook);
    }

    @Test
    void deleteBook_WhenBookExists_ShouldReturnSuccessMessage() {
        // Arrange
        int bookId = 1;
        BookEntity existingBook = new BookEntity();
        existingBook.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // Act
        ResponseEntity<String> response = underTest.deleteBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deleted book " + bookId);
        verify(bookRepository).delete(existingBook);
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.deleteBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(bookRepository, never()).delete(any(BookEntity.class));
    }

    @Test
    void deleteBook_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        int bookId = 1;
        BookEntity existingBook = new BookEntity();
        existingBook.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        doThrow(new RuntimeException("Database error")).when(bookRepository).delete(existingBook);

        // Act
        ResponseEntity<String> response = underTest.deleteBook(bookId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(bookRepository).delete(existingBook);
    }

    @Test
    void getAuthors_WhenBooksExist_ShouldReturnListOfAuthors() {
        // Arrange
        List<BookEntity> bookEntities = List.of(
                new BookEntity(1, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), true, null),
                new BookEntity(2, "Book 2", "Author 2", "Genre 2", Date.valueOf(LocalDate.now()), true, null),
                new BookEntity(3, "Book 3", "Author 1", "Genre 3", Date.valueOf(LocalDate.now()), true, null)
        );
        when(bookRepository.findAll()).thenReturn(bookEntities);

        // Act
        ResponseEntity<List<String>> response = underTest.getAuthors();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder("Author 1", "Author 2");
    }

    @Test
    void getAuthors_WhenNoBooksExist_ShouldReturnNotFound() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = underTest.getAuthors();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAuthors_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<String>> response = underTest.getAuthors();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAuthorProfile_WhenBooksExist_ShouldReturnAuthorProfile() {
        // Arrange
        String authorName = "Author 1";
        BookEntity bookEntity=new BookEntity(1, "Book 1", authorName, "Genre 1", Date.valueOf(LocalDate.now()), true, null);

        List<BookEntity> booksByAuthor = List.of(
                bookEntity,
                new BookEntity(2, "Book 2", authorName, "Genre 2", Date.valueOf(LocalDate.now()), true, null),
                new BookEntity(3, "Book 3", authorName, "Genre 1", Date.valueOf(LocalDate.now()), true, null)
        );

        List<LogEntity> logsByAuthorBooks = List.of(
                new LogEntity(1, 1, 1, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), null),
                new LogEntity(2, 2, 2, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), null),
                new LogEntity(3, 1, 3, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), null)
        );
        when(bookRepository.findByAuthor(authorName)).thenReturn(booksByAuthor);
        when(logRepository.findByBookIdIn(anyList())).thenReturn(logsByAuthorBooks);
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity));

        // Act
        ResponseEntity<AuthorProfile> response = underTest.getAuthorProfile(authorName);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(authorName);
        assertThat(response.getBody().getNoOfBooksWritten()).isEqualTo(3);
        assertThat(response.getBody().getBooksWritten()).hasSize(3);
        assertThat(response.getBody().getMostWrittenGenre()).isEqualTo("Genre 1");
        assertThat(response.getBody().getMostReadBook()).isNotNull();
        assertThat(response.getBody().getMostReadBook().getBookId()).isEqualTo(1);
    }

    @Test
    void getAuthorProfile_WhenNoBooksExist_ShouldReturnNotFound() {
        // Arrange
        String authorName = "Author 1";
        when(bookRepository.findByAuthor(authorName)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<AuthorProfile> response = underTest.getAuthorProfile(authorName);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAuthorProfile_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        String authorName = "Author 1";
        when(bookRepository.findByAuthor(authorName)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<AuthorProfile> response = underTest.getAuthorProfile(authorName);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

}