package com.hacker.boooks.controller;

import com.hacker.boooks.entity.ReviewEntity;
import com.hacker.boooks.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Review Management", description = "APIs for managing book reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "Add a review", description = "Add a review for a book if the user has borrowed it")
    @PostMapping("")
    public ResponseEntity<?> addReview(
            @RequestHeader("username") String username,
            @RequestParam int bookId,
            @RequestParam int rating,
            @RequestParam String comment) {
        return reviewService.addReview(username, bookId, rating, comment);
    }

    @Operation(summary = "Get reviews for a book", description = "Get all reviews for a specific book")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<com.hacker.boooks.bean.ReviewDTO>> getBookReviews(@PathVariable int bookId) {
        return reviewService.getBookReviews(bookId);
    }
}
