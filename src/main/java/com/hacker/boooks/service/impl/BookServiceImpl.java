package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.*;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Book management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookServiceImpl implements BookService {

    private static final String BOOK_NOT_FOUND = "Book with ID {} not found";

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    /**
     * Retrieves a list of books.
     *
     * @return ResponseEntity containing the list of books
     */
    @Override
    public ResponseEntity<List<Book>> getBooks() {

        try {
            List<Book> books = new ArrayList<>();
            List<BookEntity> bookEntities = bookRepository.findAll();

            for (BookEntity bookEntity : bookEntities) {
                Book book = new Book();
                book.setBookId(bookEntity.getBookId());
                book.setTitle(bookEntity.getTitle());
                book.setAuthor(bookEntity.getAuthor());
                book.setGenre(bookEntity.getGenre());
                book.setPublication(bookEntity.getPublication().toLocalDate());
                book.setAvailable(bookEntity.getIsAvailable());
                books.add(book);
            }

            log.debug("Retrieved {} books successfully", books.size());
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            log.error("Failed to retrieve books: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a book by ID.
     *
     * @param bookId the ID of the book to retrieve
     * @return ResponseEntity containing the book if found, or not found response if not found
     */
    @Override
    public ResponseEntity<Book> getBook(int bookId) {

        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                log.debug(BOOK_NOT_FOUND, bookId);
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            Book book = new Book();
            book.setBookId(bookEntity.getBookId());
            book.setTitle(bookEntity.getTitle());
            book.setAuthor(bookEntity.getAuthor());
            book.setPublication(bookEntity.getPublication().toLocalDate());
            book.setAvailable(bookEntity.getIsAvailable());
            book.setHolder(bookEntity.getHolder());

            log.debug("Retrieved book with ID {}: {}", bookId, book);
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            log.error("Failed to retrieve book with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the profile of a book.
     *
     * @param bookId the ID of the book to retrieve the profile for
     * @return ResponseEntity containing the book profile if found, or not found response if not found
     */
    @Override
    public ResponseEntity<BookProfile> getBookProfile(int bookId) {

        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                log.debug(BOOK_NOT_FOUND, bookId);
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();

            BookProfile bookProfile = new BookProfile();
            bookProfile.setTitle(bookEntity.getTitle());
            bookProfile.setAuthor(bookEntity.getAuthor());
            bookProfile.setGenre(bookEntity.getGenre());
            bookProfile.setAvailable(bookEntity.getIsAvailable());

            if (Boolean.FALSE.equals(bookEntity.getIsAvailable())) {
                Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(bookEntity.getHolder());
                if (optionalMemberEntity.isPresent()) {
                    MemberEntity memberEntity = optionalMemberEntity.get();
                    Member member = new Member(memberEntity.getMemberId(), memberEntity.getName(), memberEntity.getEmail(), memberEntity.getPhoneNumber());
                    bookProfile.setHolder(member);
                }
            }

            log.debug("Retrieved book profile with ID {}: {}", bookId, bookProfile);
            return ResponseEntity.ok(bookProfile);
        } catch (Exception e) {
            log.error("Failed to retrieve book profile with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a new book to the library.
     *
     * @param bookBO the book business object containing the details of the book to be added
     * @return ResponseEntity indicating the success or failure of the book addition
     */
    @Override
    public ResponseEntity<String> addBook(BookBO bookBO) {

        try {
            BookEntity bookEntity = new BookEntity();
            bookEntity.setTitle(bookBO.getTitle());
            bookEntity.setAuthor(bookBO.getAuthor());
            bookEntity.setGenre(bookBO.getGenre());
            bookEntity.setPublication(Date.valueOf(bookBO.getPublicationDate()));

            bookRepository.save(bookEntity);

            log.debug("Added a new book: {}", bookEntity);
            return ResponseEntity.ok("Book added successfully");
        } catch (Exception e) {
            log.error("Failed to add book: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates a book in the library.
     *
     * @param bookId the ID of the book to be updated
     * @param bookBO the book business object containing the updated details
     * @return ResponseEntity indicating the success or failure of the book update
     */
    @Override
    public ResponseEntity<String> updateBook(int bookId, BookBO bookBO) {

        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                log.debug(BOOK_NOT_FOUND, bookId);
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            bookEntity.setTitle(bookBO.getTitle());
            bookEntity.setAuthor(bookBO.getAuthor());
            bookEntity.setGenre(bookBO.getGenre());
            bookEntity.setPublication(Date.valueOf(bookBO.getPublicationDate()));

            bookRepository.save(bookEntity);

            log.debug("Updated book with ID {}: {}", bookId, bookEntity);
            return ResponseEntity.ok("Book updated successfully");
        } catch (Exception e) {
            log.error("Failed to update book with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a book from the library.
     *
     * @param bookId the ID of the book to be deleted
     * @return ResponseEntity indicating the success or failure of the book deletion
     */
    @Override
    public ResponseEntity<String> deleteBook(int bookId) {

        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                log.debug(BOOK_NOT_FOUND, bookId);
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            bookRepository.delete(bookEntity);

            log.debug("Deleted book with ID {}", bookId);
            return ResponseEntity.ok("Deleted book " + bookId);
        } catch (Exception e) {
            log.error("Failed to delete book with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the list of authors.
     *
     * @return The list of authors.
     */
    @Override
    public ResponseEntity<List<String>> getAuthors() {
        try {
            List<BookEntity> bookEntities = bookRepository.findAll();
            if (bookEntities.isEmpty()) {
                log.debug("No authors found");
                return ResponseEntity.notFound().build();
            }

            List<String> authors = bookEntities.stream()
                    .map(BookEntity::getAuthor)
                    .distinct()
                    .toList();

            log.debug("Authors fetched successfully");
            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            log.error("Error occurred while fetching authors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the profile of an author based on their name.
     * This API provides information such as the author's name, the number of books written by the author,
     * the most written genre by the author, a list of books written by the author, and the most read book
     * by the author's audience.
     *
     * @param name The name of the author.
     * @return The author's profile.
     */
    @Override
    public ResponseEntity<AuthorProfile> getAuthorProfile(String name) {

        try {
            AuthorProfile authorProfile = new AuthorProfile();
            List<Book> books = new ArrayList<>();

            List<BookEntity> booksByAuthor = bookRepository.findByAuthor(name);
            if (booksByAuthor.isEmpty()) {
                log.debug("No books found for author: {}", name);
                return ResponseEntity.notFound().build();
            }

            authorProfile.setName(name);
            authorProfile.setNoOfBooksWritten(booksByAuthor.size());

            for (BookEntity bookEntity : booksByAuthor) {
                Book book = new Book();
                book.setBookId(bookEntity.getBookId());
                book.setTitle(bookEntity.getTitle());
                book.setAuthor(bookEntity.getAuthor());
                book.setGenre(bookEntity.getGenre());
                book.setPublication(bookEntity.getPublication().toLocalDate());
                book.setAvailable(bookEntity.getIsAvailable());
                books.add(book);
            }

            authorProfile.setBooksWritten(books);

            // Count the occurrences of each genre
            Map<String, Long> genreCounts = booksByAuthor.stream()
                    .collect(Collectors.groupingBy(BookEntity::getGenre, Collectors.counting()));

            // Find the genre with the highest count
            String mostOccurringGenre = genreCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            authorProfile.setMostWrittenGenre(mostOccurringGenre);

            List<LogEntity> logsByAuthorBooks = logRepository.findByBookIdIn(
                    booksByAuthor.stream()
                            .map(BookEntity::getBookId)
                            .toList()
            );

            Map<Integer, Long> bookCounts = logsByAuthorBooks.stream()
                    .collect(Collectors.groupingBy(LogEntity::getBookId, Collectors.counting()));

            Integer mostOccurringBookId = bookCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (mostOccurringBookId != null) {
                Optional<BookEntity> optionalBookEntity = bookRepository.findById(mostOccurringBookId);

                if (optionalBookEntity.isPresent()) {
                    BookEntity bookEntity = optionalBookEntity.get();
                    Book book = new Book();
                    book.setBookId(bookEntity.getBookId());
                    book.setTitle(bookEntity.getTitle());
                    book.setAuthor(bookEntity.getAuthor());
                    book.setGenre(bookEntity.getGenre());
                    book.setPublication(bookEntity.getPublication().toLocalDate());
                    book.setAvailable(bookEntity.getIsAvailable());
                    authorProfile.setMostReadBook(book);
                }
            }

            log.debug("Author profile retrieved for author: {}", name);
            return ResponseEntity.ok(authorProfile);
        } catch (Exception e) {
            log.error("Error occurred while fetching author profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
