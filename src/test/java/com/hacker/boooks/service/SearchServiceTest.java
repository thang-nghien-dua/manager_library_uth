package com.hacker.boooks.service;

import com.hacker.boooks.bean.SearchResponse;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SearchServiceImpl underTest;

    @Test
    void search_KeywordMatchesBooksAndMembers_ReturnsSearchResponses() {
        // Arrange
        String keyword = "java";

        List<BookEntity> bookEntities = new ArrayList<>();
        bookEntities.add(new BookEntity(1, "Java Programming", "John Doe", "Genre 1", Date.valueOf(LocalDate.now()), true, null));
        bookEntities.add(new BookEntity(2, "Introduction to Java", "Jane Smith", "Genre 2", Date.valueOf(LocalDate.now()), false, 1));
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword))
                .thenReturn(bookEntities);

        List<MemberEntity> memberEntities = new ArrayList<>();
        memberEntities.add(new MemberEntity(1, "John Doe", "john@example.com", "1234567890"));
        when(memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(keyword, keyword, keyword))
                .thenReturn(memberEntities);

        // Act
        ResponseEntity<List<SearchResponse>> response = underTest.search(keyword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<SearchResponse> searchResponses = response.getBody();
        assertEquals(3, searchResponses.size()); // 2 books + 1 member

        SearchResponse bookResponse1 = searchResponses.get(0);
        assertEquals("1", bookResponse1.getValue());
        assertEquals("Book", bookResponse1.getType());

        SearchResponse bookResponse2 = searchResponses.get(1);
        assertEquals("2", bookResponse2.getValue());
        assertEquals("Book", bookResponse2.getType());

        SearchResponse memberResponse = searchResponses.get(2);
        assertEquals("1", memberResponse.getValue());
        assertEquals("Member", memberResponse.getType());
    }

    @Test
    void search_NoMatches_ReturnsEmptyList() {
        // Arrange
        String keyword = "python";

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword))
                .thenReturn(new ArrayList<>());

        when(memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(keyword, keyword, keyword))
                .thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<SearchResponse>> response = underTest.search(keyword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

    }

    @Test
    void search_ExceptionThrown_ReturnsInternalServerErrorResponse() {
        // Arrange
        String keyword = "java";

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<SearchResponse>> response = underTest.search(keyword);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}