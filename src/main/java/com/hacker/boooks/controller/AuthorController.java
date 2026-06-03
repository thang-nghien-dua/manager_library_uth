package com.hacker.boooks.controller;

import com.hacker.boooks.bean.AuthorProfile;
import com.hacker.boooks.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/author")
@Tag(name = "Author Management", description = "APIs for managing authors")
@CrossOrigin(origins = "*")
@SuppressWarnings("unused")
public class AuthorController {

    @Autowired
    private BookService bookService;

    @Operation(summary = "Get Authors", description = "Retrieve the list of authors.")
    @GetMapping("")
    public ResponseEntity<List<String>> getAuthors() {
        return bookService.getAuthors();
    }

    @Operation(summary = "Get author Profile", description = "This API allows you to retrieve the profile of an author based on their name. It provides information such as the author's name, the number of books written by the author, the most written genre by the author, a list of books written by the author, and the most read book by the author's audience. Use this API to gain insights into an author's background and literary accomplishments.")
    @GetMapping("/{name}/profile")
    public ResponseEntity<AuthorProfile> getAuthorProfile(@PathVariable String name) {
        return bookService.getAuthorProfile(name);
    }

}
