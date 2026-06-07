package com.hacker.boooks.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() { return "login"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/books-ui")
    public String books() { return "books"; }

    @GetMapping("/members-ui")
    public String members() { return "members"; }

    @GetMapping("/issue-return")
    public String issueReturn() { return "issue_return"; }

    @GetMapping("/user/home")
    public String userHome() { return "user_home"; }

    @GetMapping("/user/book-details")
    public String userBookDetails() { return "book_details"; }

    @GetMapping("/user/history")
    public String userHistory() { return "my_history"; }
}