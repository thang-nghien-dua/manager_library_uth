package com.hacker.boooks.service;

import com.hacker.boooks.bean.AuthorProfile;
import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.BookBO;
import com.hacker.boooks.bean.BookProfile;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Book management service.
 */
public interface BookService {

    /**
     * Retrieves a list of books.
     *
     * @return ResponseEntity containing the list of books
     */
    ResponseEntity<List<Book>> getBooks();

    /**
     * Retrieves a book by ID.
     *
     * @param bookId the ID of the book to retrieve
     * @return ResponseEntity containing the book if found, or not found response if not found
     */
    ResponseEntity<Book> getBook(int bookId);

    /**
     * Retrieves the profile of a book.
     *
     * @param bookId the ID of the book to retrieve the profile for
     * @return ResponseEntity containing the book profile if found, or not found response if not found
     */
    ResponseEntity<BookProfile> getBookProfile(int bookId);

    /**
     * Adds a new book to the library.
     *
     * @param bookBO the book business object containing the details of the book to be added
     * @return ResponseEntity indicating the success or failure of the book addition
     */
    ResponseEntity<String> addBook(BookBO bookBO);

    /**
     * Updates a book in the library.
     *
     * @param bookId the ID of the book to be updated
     * @param bookBO the book business object containing the updated details
     * @return ResponseEntity indicating the success or failure of the book update
     */
    ResponseEntity<String> updateBook(int bookId, BookBO bookBO);

    /**
     * Deletes a book from the library.
     *
     * @param bookId the ID of the book to be deleted
     * @return ResponseEntity indicating the success or failure of the book deletion
     */
    ResponseEntity<String> deleteBook(int bookId);

    /**
     * Get the list of authors.
     *
     * @return The list of authors.
     */
    ResponseEntity<List<String>> getAuthors();

    /**
     * Get the profile of an author based on their name.
     * This API provides information such as the author's name, the number of books written by the author,
     * the most written genre by the author, a list of books written by the author, and the most read book
     * by the author's audience.
     *
     * @param name The name of the author.
     * @return The author's profile.
     */
    ResponseEntity<AuthorProfile> getAuthorProfile(String name);

}
