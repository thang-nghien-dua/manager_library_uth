package com.hacker.boooks.service.impl;

import com.hacker.boooks.entity.BookCopyEntity;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.entity.ReviewEntity;
import com.hacker.boooks.repository.BookCopyRepository;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.repository.ReviewRepository;
import com.hacker.boooks.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    public ResponseEntity<?> addReview(String username, int bookId, int rating, String comment) {
        try {
            MemberEntity member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

            List<BookCopyEntity> copies = bookCopyRepository.findByBookId(bookId);
            List<Integer> copyIds = copies.stream().map(BookCopyEntity::getCopyId).collect(Collectors.toList());
            
            if (copyIds.isEmpty()) {
                throw new IllegalArgumentException("Sách này không tồn tại.");
            }

            List<LogEntity> logs = logRepository.findByCopyIdIn(copyIds);
            boolean hasBorrowed = logs.stream().anyMatch(l -> l.getMemberId().equals(member.getMemberId()));
            
            if (!hasBorrowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn chỉ có thể đánh giá sách sau khi đã mượn.");
            }

            if (reviewRepository.existsByBookIdAndMemberId(bookId, member.getMemberId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bạn đã đánh giá sách này rồi.");
            }

            ReviewEntity review = new ReviewEntity();
            review.setBookId(bookId);
            review.setMemberId(member.getMemberId());
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            reviewRepository.save(review);
            return ResponseEntity.ok("Đánh giá thành công.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to add review", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<com.hacker.boooks.bean.ReviewDTO>> getBookReviews(int bookId) {
        List<ReviewEntity> reviews = reviewRepository.findByBookId(bookId);
        List<com.hacker.boooks.bean.ReviewDTO> reviewDTOs = reviews.stream().map(r -> {
            com.hacker.boooks.bean.ReviewDTO dto = new com.hacker.boooks.bean.ReviewDTO();
            dto.setReviewId(r.getReviewId());
            dto.setBookId(r.getBookId());
            dto.setMemberId(r.getMemberId());
            dto.setRating(r.getRating());
            dto.setComment(r.getComment());
            dto.setCreatedAt(r.getCreatedAt());
            
            memberRepository.findById(r.getMemberId()).ifPresent(m -> dto.setUsername(m.getUsername()));
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(reviewDTOs);
    }
}
