package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.*;
import com.hacker.boooks.entity.*;
import com.hacker.boooks.repository.*;
import com.hacker.boooks.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    public ResponseEntity<List<Book>> getBooks() {
        try {
            List<Book> books = new ArrayList<>();
            List<BookEntity> bookEntities = bookRepository.findAll();

            for (BookEntity bookEntity : bookEntities) {
                Book book = new Book();
                book.setBookId(bookEntity.getBookId());
                book.setTitle(bookEntity.getTitle());
                
                String authors = bookEntity.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
                book.setAuthor(authors.isEmpty() ? "Unknown" : authors);
                
                String genres = bookEntity.getCategories().stream().map(CategoryEntity::getName).collect(Collectors.joining(", "));
                book.setGenre(genres.isEmpty() ? "Unknown" : genres);
                
                if (bookEntity.getPublication() != null) {
                    book.setPublication(bookEntity.getPublication().toLocalDate());
                }
                
                book.setPageCount(bookEntity.getPageCount());
                book.setDescription(bookEntity.getDescription());
                book.setCoverImage(bookEntity.getCoverImage());
                
                // Get copies status
                List<BookCopyEntity> copies = bookCopyRepository.findByBookId(bookEntity.getBookId());
                long availableCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.AVAILABLE).count();
                long rentedCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.BORROWED).count();
                
                book.setQuantityAvailable((int) availableCount);
                book.setQuantityRented((int) rentedCount);
                book.setAvailable(availableCount > 0);
                
                books.add(book);
            }
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            log.error("Failed to retrieve books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Book> getBook(int bookId) {
        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            Book book = new Book();
            book.setBookId(bookEntity.getBookId());
            book.setTitle(bookEntity.getTitle());
            String authors = bookEntity.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
            book.setAuthor(authors);
            String genres = bookEntity.getCategories().stream().map(CategoryEntity::getName).collect(Collectors.joining(", "));
            book.setGenre(genres);
            if (bookEntity.getPublication() != null) {
                book.setPublication(bookEntity.getPublication().toLocalDate());
            }

            book.setPageCount(bookEntity.getPageCount());
            book.setDescription(bookEntity.getDescription());
            book.setCoverImage(bookEntity.getCoverImage());

            List<BookCopyEntity> copies = bookCopyRepository.findByBookId(bookEntity.getBookId());
            long availableCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.AVAILABLE).count();
            long rentedCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.BORROWED).count();
            
            book.setQuantityAvailable((int) availableCount);
            book.setQuantityRented((int) rentedCount);
            book.setAvailable(availableCount > 0);

            return ResponseEntity.ok(book);
        } catch (Exception e) {
            log.error("Failed to retrieve book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<BookProfile> getBookProfile(int bookId) {
        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            BookProfile bookProfile = new BookProfile();
            bookProfile.setTitle(bookEntity.getTitle());
            String authors = bookEntity.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
            bookProfile.setAuthor(authors);
            String genres = bookEntity.getCategories().stream().map(CategoryEntity::getName).collect(Collectors.joining(", "));
            bookProfile.setGenre(genres);

            bookProfile.setPageCount(bookEntity.getPageCount());
            bookProfile.setDescription(bookEntity.getDescription());
            bookProfile.setCoverImage(bookEntity.getCoverImage());

            List<BookCopyEntity> copies = bookCopyRepository.findByBookId(bookEntity.getBookId());
            long availableCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.AVAILABLE).count();
            long rentedCount = copies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.BORROWED).count();
            
            bookProfile.setQuantityAvailable((int) availableCount);
            bookProfile.setQuantityRented((int) rentedCount);
            bookProfile.setAvailable(availableCount > 0);

            return ResponseEntity.ok(bookProfile);
        } catch (Exception e) {
            log.error("Failed to retrieve book profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> addBook(BookBO bookBO) {
        try {
            // Validate unique book (title + author)
            List<BookEntity> existingBooks = bookRepository.findByTitleIgnoreCase(bookBO.getTitle());
            for (BookEntity existing : existingBooks) {
                String existingAuthors = existing.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(","));
                if (bookBO.getAuthor() != null && existingAuthors.equalsIgnoreCase(bookBO.getAuthor().trim())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sách đã tồn tại (trùng tiêu đề và tác giả)");
                }
            }

            BookEntity bookEntity = new BookEntity();
            bookEntity.setTitle(bookBO.getTitle());
            if (bookBO.getPublicationDate() != null) {
                bookEntity.setPublication(Date.valueOf(bookBO.getPublicationDate()));
            }
            bookEntity.setPageCount(bookBO.getPageCount());
            bookEntity.setDescription(bookBO.getDescription());
            bookEntity.setCoverImage(bookBO.getCoverImage());

            // Handle author mapping (find-or-create)
            List<AuthorEntity> authorList = new ArrayList<>();
            if (bookBO.getAuthor() != null) {
                String[] names = bookBO.getAuthor().split(",");
                for (String name : names) {
                    final String trimmedName = name.trim();
                    if (!trimmedName.isEmpty()) {
                        AuthorEntity author = authorRepository.findByNameIgnoreCase(trimmedName)
                                .orElseGet(() -> authorRepository.save(new AuthorEntity(null, trimmedName, "")));
                        authorList.add(author);
                    }
                }
            }
            bookEntity.setAuthors(authorList);

            // Handle category mapping (find-or-create)
            List<CategoryEntity> categoryList = new ArrayList<>();
            if (bookBO.getGenre() != null) {
                String[] names = bookBO.getGenre().split(",");
                for (String name : names) {
                    final String trimmedName = name.trim();
                    if (!trimmedName.isEmpty()) {
                        CategoryEntity cat = categoryRepository.findByNameIgnoreCase(trimmedName)
                                .orElseGet(() -> categoryRepository.save(new CategoryEntity(null, trimmedName)));
                        categoryList.add(cat);
                    }
                }
            }
            bookEntity.setCategories(categoryList);

            BookEntity savedBook = bookRepository.save(bookEntity);

            // Add requested quantity of copies
            int quantity = bookBO.getQuantity() > 0 ? bookBO.getQuantity() : 1;
            for (int i = 0; i < quantity; i++) {
                BookCopyEntity copy = new BookCopyEntity(null, savedBook.getBookId(), BookCopyEntity.Status.AVAILABLE);
                bookCopyRepository.save(copy);
            }

            return ResponseEntity.ok("Book added successfully");
        } catch (Exception e) {
            log.error("Failed to add book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateBook(int bookId, BookBO bookBO) {
        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            bookEntity.setTitle(bookBO.getTitle());
            if (bookBO.getPublicationDate() != null) {
                bookEntity.setPublication(Date.valueOf(bookBO.getPublicationDate()));
            }
            bookEntity.setPageCount(bookBO.getPageCount());
            bookEntity.setDescription(bookBO.getDescription());
            if (bookBO.getCoverImage() != null) {
                bookEntity.setCoverImage(bookBO.getCoverImage());
            }

            // Update authors
            List<AuthorEntity> authorList = new ArrayList<>();
            if (bookBO.getAuthor() != null) {
                String[] names = bookBO.getAuthor().split(",");
                for (String name : names) {
                    final String trimmedName = name.trim();
                    if (!trimmedName.isEmpty()) {
                        AuthorEntity author = authorRepository.findByNameIgnoreCase(trimmedName)
                                .orElseGet(() -> authorRepository.save(new AuthorEntity(null, trimmedName, "")));
                        authorList.add(author);
                    }
                }
            }
            bookEntity.setAuthors(authorList);

            // Update categories
            List<CategoryEntity> categoryList = new ArrayList<>();
            if (bookBO.getGenre() != null) {
                String[] names = bookBO.getGenre().split(",");
                for (String name : names) {
                    final String trimmedName = name.trim();
                    if (!trimmedName.isEmpty()) {
                        CategoryEntity cat = categoryRepository.findByNameIgnoreCase(trimmedName)
                                .orElseGet(() -> categoryRepository.save(new CategoryEntity(null, trimmedName)));
                        categoryList.add(cat);
                    }
                }
            }
            bookEntity.setCategories(categoryList);

            bookRepository.save(bookEntity);

            // Handle updating book quantity (copies)
            List<BookCopyEntity> currentCopies = bookCopyRepository.findByBookId(bookEntity.getBookId());
            long rentedCount = currentCopies.stream().filter(c -> c.getStatus() == BookCopyEntity.Status.BORROWED).count();
            int requestedQuantity = bookBO.getQuantity() > 0 ? bookBO.getQuantity() : 1;
            
            if (requestedQuantity < rentedCount) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể giảm số lượng xuống " + requestedQuantity + " vì đang có " + rentedCount + " cuốn đang được mượn.");
            }
            
            if (requestedQuantity > currentCopies.size()) {
                // Add more copies
                int toAdd = requestedQuantity - currentCopies.size();
                for (int i = 0; i < toAdd; i++) {
                    bookCopyRepository.save(new BookCopyEntity(null, bookEntity.getBookId(), BookCopyEntity.Status.AVAILABLE));
                }
            } else if (requestedQuantity < currentCopies.size()) {
                // Remove available copies
                int toRemove = currentCopies.size() - requestedQuantity;
                int removed = 0;
                for (BookCopyEntity copy : currentCopies) {
                    if (copy.getStatus() == BookCopyEntity.Status.AVAILABLE && removed < toRemove) {
                        bookCopyRepository.delete(copy);
                        removed++;
                    }
                }
            }

            return ResponseEntity.ok("Book updated successfully");
        } catch (Exception e) {
            log.error("Failed to update book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteBook(int bookId) {
        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            if (optionalBookEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            bookRepository.delete(optionalBookEntity.get());
            return ResponseEntity.ok("Book deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getAuthors() {
        try {
            List<String> authors = authorRepository.findAll().stream().map(AuthorEntity::getName).collect(Collectors.toList());
            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<AuthorProfile> getAuthorProfile(String name) {
        try {
            Optional<AuthorEntity> optionalAuthor = authorRepository.findByNameIgnoreCase(name);
            if (optionalAuthor.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            AuthorEntity authorEntity = optionalAuthor.get();
            List<BookEntity> booksByAuthor = bookRepository.findByAuthor(name);

            AuthorProfile authorProfile = new AuthorProfile();
            authorProfile.setName(authorEntity.getName());
            authorProfile.setNoOfBooksWritten(booksByAuthor.size());

            List<Book> books = booksByAuthor.stream().map(be -> {
                Book b = new Book();
                b.setBookId(be.getBookId());
                b.setTitle(be.getTitle());
                b.setAuthor(authorEntity.getName());
                return b;
            }).collect(Collectors.toList());
            authorProfile.setBooksWritten(books);

            return ResponseEntity.ok(authorProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}