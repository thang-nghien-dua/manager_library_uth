package com.hacker.boooks.service;

import com.hacker.boooks.entity.ReviewEntity;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ReviewService {
    ResponseEntity<?> addReview(String username, int bookId, int rating, String comment);
    ResponseEntity<List<ReviewEntity>> getBookReviews(int bookId);
}
