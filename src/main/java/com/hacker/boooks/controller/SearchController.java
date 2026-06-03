package com.hacker.boooks.controller;

import com.hacker.boooks.bean.SearchResponse;
import com.hacker.boooks.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@Tag(name = "Global Search", description = "API for searching books, authors, and members in the library management system")
@CrossOrigin(origins = "*")
@Slf4j
@SuppressWarnings("unused")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Operation(summary = "Search", description = "This API allows you to perform a global search in the library management system based on a keyword. It retrieves search results for books, authors, and members that match the provided keyword. Use this API to find relevant information in the library management system.")
    @GetMapping("/{keyword}")
    public ResponseEntity<List<SearchResponse>> search(@PathVariable String keyword) {
        return searchService.search(keyword);
    }

}
