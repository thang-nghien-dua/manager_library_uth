package com.hacker.boooks.controller;

import com.hacker.boooks.bean.IssueResponse;
import com.hacker.boooks.bean.ReturnResponse;
import com.hacker.boooks.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@Tag(name = "Book Management", description = "APIs for managing books")
@CrossOrigin(origins = "*")
@SuppressWarnings("unused")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Operation(summary = "Issue Book", description = "This API allows you to issue a book to a member in the library system. It takes the book ID and member ID as input parameters and returns the response containing the details of the issued book.")
    @PostMapping("/issue")
    public ResponseEntity<IssueResponse> issueBook(@RequestParam int bookId, @RequestParam int memberId) {
        return issueService.issueBook(bookId, memberId);
    }

    @Operation(summary = "Return Book", description = "This API allows you to return a book that was previously issued by a member. It takes the book ID and member ID as input parameters and returns the response containing the details of the returned book.")
    @PostMapping("/return")
    public ResponseEntity<ReturnResponse> returnBook(@RequestParam int bookId, @RequestParam int memberId) {
        return issueService.returnBook(bookId, memberId);
    }

}
