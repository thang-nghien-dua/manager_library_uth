package com.hacker.boooks.service;

import com.hacker.boooks.bean.ReviewDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReviewService {

    ResponseEntity<?> addReview(String username, int bookId, int rating, String comment);

    ResponseEntity<List<ReviewDTO>> getBookReviews(int bookId);

}
