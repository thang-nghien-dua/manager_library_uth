package com.hacker.boooks.service;

import com.hacker.boooks.bean.IssueResponse;
import com.hacker.boooks.bean.ReturnResponse;
import org.springframework.http.ResponseEntity;

/**
 * Book issue management service.
 */
public interface IssueService {

    /**
     * Issues a book to a member and returns the expected return date.
     *
     * @param bookId   The ID of the book to be issued.
     * @param memberId The ID of the member to whom the book is issued.
     * @return ResponseEntity containing the issue response with the expected return date.
     */
    ResponseEntity<IssueResponse> issueBook(int bookId, int memberId);

    /**
     * Returns a book and calculates the fine (if any) for the given book and member.
     *
     * @param bookId   The ID of the book to be returned.
     * @param memberId The ID of the member returning the book.
     * @return ResponseEntity containing the return response with details such as return date, expected return date, and total fine (if applicable).
     */
    ResponseEntity<ReturnResponse> returnBook(int bookId, int memberId);

}
