package com.hacker.boooks.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer reviewId;
    private Integer bookId;
    private Integer memberId;
    private String username;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;
}
