package com.hacker.boooks.controller;

import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.BookBO;
import com.hacker.boooks.bean.BookProfile;
import com.hacker.boooks.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/books")
@Tag(name = "Book Management", description = "APIs for managing books")
@CrossOrigin(origins = "*")
@SuppressWarnings("unused")
public class BookController {

    @Autowired
    private BookService bookService;


    @Operation(summary = "Get all books", description = "This API allows you to retrieve a list of all books available in the library. It returns information such as book titles, authors, and availability.")
    @GetMapping("")
    public ResponseEntity<List<Book>> getBooks() {
        return bookService.getBooks();
    }

    @Operation(summary = "Get a book by ID", description = "This API allows you to retrieve a list of all books available in the library. It returns information such as book titles, authors, and availability.")
    @GetMapping("/{bookId}")
    public ResponseEntity<Book> getBook(@PathVariable int bookId) {
        return bookService.getBook(bookId);
    }

    @Operation(summary = "Get book profile", description = "This API provides a comprehensive profile of a specific book. It includes detailed information about the book, such as the title, author, genre, publication date, ISBN, availability, and any additional details.")
    @GetMapping("/{bookId}/profile")
    public ResponseEntity<BookProfile> getBookProfile(@PathVariable int bookId) {
        return bookService.getBookProfile(bookId);
    }

    @Operation(summary = "Add a new book", description = "Use this API to add a new book to the library database. You need to provide the necessary details of the book, including the title, author, genre, publication date, and any other relevant information. After successful execution, the book will be added to the library collection.")
    @PostMapping("")
    public ResponseEntity<String> addBook(@RequestParam String title, @RequestParam String author, @RequestParam String genre, @Parameter(example = "2023-05-30") @RequestParam String publishedOn) {
        LocalDate publishedDate = LocalDate.parse(publishedOn, DateTimeFormatter.ISO_DATE);
        BookBO bookBO = new BookBO(title, author, genre, publishedDate);
        return bookService.addBook(bookBO);
    }

    @Operation(summary = "Update book details", description = "This API allows you to update the details of a specific book. You need to provide the book's ID along with the updated information, such as the title, author, genre, publication date, or any other relevant fields. After executing the request, the book's details will be updated in the library system.")
    @PutMapping("/{bookId}")
    public ResponseEntity<String> updateBook(@PathVariable int bookId, @RequestParam String title, @RequestParam String author, @RequestParam String genre, @RequestParam String publishedOn) {
        LocalDate publishedDate = LocalDate.parse(publishedOn, DateTimeFormatter.ISO_DATE);
        BookBO bookBO = new BookBO(title, author, genre, publishedDate);
        return bookService.updateBook(bookId, bookBO);
    }

    @Operation(summary = "Delete a book", description = "Use this API to remove a specific book from the library collection. You need to specify the book's ID, and upon successful execution, the book will be permanently deleted from the library system.")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable int bookId) {
        return bookService.deleteBook(bookId);
    }

}
