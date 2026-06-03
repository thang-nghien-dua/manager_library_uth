package com.hacker.boooks.service;

import com.hacker.boooks.bean.SearchResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SearchService {

    /**
     * Search for book Ids and member Ids based on the provided keyword.
     *
     * @param keyword The keyword to search for.
     * @return ResponseEntity containing a list of SearchResponse objects.
     */
    ResponseEntity<List<SearchResponse>> search(String keyword);

}
