package com.hacker.boooks.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() { return "login"; }

    @GetMapping("/landing")
    public String landing() { return "landing"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/books-ui")
    public String books() { return "books"; }

    @GetMapping("/members-ui")
    public String members() { return "members"; }

    @GetMapping("/issue-return")
    public String issueReturn() { return "issue_return"; }

    @GetMapping("/reports")
    public String reports() { return "reports"; }

    @GetMapping("/admin/staff")
    public String adminStaff() { return "admin_staff"; }

    @GetMapping("/user/home")
    public String userHome() { return "user_home"; }

    @GetMapping("/user/book-details")
    public String userBookDetails() { return "book_details"; }

    @GetMapping("/user/history")
    public String userHistory() { return "my_history"; }

    @GetMapping("/user/profile")
    public String userProfile() { return "profile"; }
}