package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.SearchResponse;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class SearchServiceImpl implements SearchService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    /**
     * Search for book Ids and member Ids based on the provided keyword.
     *
     * @param keyword The keyword to search for.
     * @return ResponseEntity containing a list of SearchResponse objects.
     */
    @Override
    public ResponseEntity<List<SearchResponse>> search(String keyword) {
        try {
            List<BookEntity> bookEntities = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
            List<MemberEntity> memberEntities = memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(keyword, keyword, keyword);

            log.info("Number of books fetched: {}", bookEntities.size());
            log.info("Number of members fetched: {}", memberEntities.size());

            List<SearchResponse> searchResponses = new ArrayList<>();

            for (BookEntity bookEntity : bookEntities) {
                SearchResponse searchResponse = new SearchResponse(bookEntity.getBookId().toString(), "Book");
                searchResponses.add(searchResponse);
            }

            for (MemberEntity memberEntity : memberEntities) {
                SearchResponse searchResponse = new SearchResponse(memberEntity.getMemberId().toString(), "Member");
                searchResponses.add(searchResponse);
            }

            return ResponseEntity.ok(searchResponses);
        } catch (Exception e) {
            log.error("Error occurred while performing search: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
