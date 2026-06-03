package com.hacker.boooks.service;

import com.hacker.boooks.bean.IssueResponse;
import com.hacker.boooks.bean.ReturnResponse;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.FineEntity;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.FineRepository;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.impl.IssueServiceImpl;
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
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IssueServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private LogRepository logRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private IssueServiceImpl underTest;

    @Test
    void testIssueBook_WithExistingBookAndMember_ReturnsExpectedReturnDate() {
        // Arrange
        BookEntity bookEntity = new BookEntity();
        bookEntity.setIsAvailable(true);
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity));

        MemberEntity memberEntity = new MemberEntity();
        when(memberRepository.findById(1)).thenReturn(Optional.of(memberEntity));

        FineEntity fineEntity = new FineEntity(1, 3, 10.0f);
        when(fineRepository.findById(1)).thenReturn(Optional.of(fineEntity));

        when(logRepository.save(any())).thenReturn(new LogEntity());

        // Act
        ResponseEntity<IssueResponse> response = underTest.issueBook(1, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LocalDate.now().plusDays(fineEntity.getDaysOverdue()), Objects.requireNonNull(response.getBody()).getExpectedReturnDate());
        assertEquals(false, bookEntity.getIsAvailable());
        assertEquals(1, bookEntity.getHolder());
        verify(bookRepository, times(1)).save(bookEntity);
        verify(logRepository, times(1)).save(any());
    }

    @Test
    void testIssueBook_WithNonExistingBook_ReturnsNotFound() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        MemberEntity memberEntity = new MemberEntity();
        when(memberRepository.findById(1)).thenReturn(Optional.of(memberEntity));

        // Act
        ResponseEntity<IssueResponse> response = underTest.issueBook(1, 1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void testIssueBook_WithNonExistingMember_ReturnsNotFound() {
        // Arrange
        BookEntity bookEntity = new BookEntity();
        bookEntity.setIsAvailable(true);
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity));

        when(memberRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<IssueResponse> response = underTest.issueBook(1, 1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void testIssueBook_ExceptionThrown_ReturnsInternalServerError() {
        // Arrange
        when(bookRepository.findById(1)).thenThrow(new RuntimeException("Database connection error"));

        MemberEntity memberEntity = new MemberEntity();
        when(memberRepository.findById(1)).thenReturn(Optional.of(memberEntity));

        // Act
        ResponseEntity<IssueResponse> response = underTest.issueBook(1, 1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void testReturnBook_WithExistingLogEntityAndBook_ReturnsReturnResponse() {
        // Arrange
        LogEntity logEntity = new LogEntity();
        logEntity.setMemberId(1);
        logEntity.setBookId(1);
        logEntity.setIssueDate(Date.valueOf(LocalDate.now().minusDays(5)));
        when(logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(1, 1)).thenReturn(Optional.of(logEntity));

        BookEntity bookEntity = new BookEntity();
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity));

        FineEntity fineEntity = new FineEntity(1, 3, 10.0f);
        when(fineRepository.findById(1)).thenReturn(Optional.of(fineEntity));

        LocalDate currentDate = LocalDate.now();
        LocalDate expectedReturnDate = currentDate.minusDays(5).plusDays(fineEntity.getDaysOverdue());
        float totalFine = fineEntity.getFineAmount() * 2; // Assuming 2 days overdue

        // Act
        ResponseEntity<ReturnResponse> response = underTest.returnBook(1, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(currentDate, Objects.requireNonNull(response.getBody()).getActualReturnDate());
        assertEquals(expectedReturnDate, response.getBody().getExpectedReturnDate());
        assertEquals(totalFine, response.getBody().getFine());
        assertEquals(true, bookEntity.getIsAvailable());
        assertNull(bookEntity.getHolder());
        assertEquals(Date.valueOf(currentDate), logEntity.getReturnDate());
        assertEquals(totalFine, logEntity.getFine());
        verify(bookRepository, times(1)).save(bookEntity);
        verify(logRepository, times(1)).save(logEntity);
    }

    @Test
    void testReturnBook_WithNonExistingLogEntity_ReturnsNotFound() {
        // Arrange
        when(logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(1, 1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ReturnResponse> response = underTest.returnBook(1, 1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void testReturnBook_WithExistingBookAndNoFine_ReturnsReturnResponseWithZeroFine() {
        // Arrange
        LogEntity logEntity = new LogEntity();
        logEntity.setMemberId(1);
        logEntity.setBookId(1);
        when(logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(1, 1)).thenReturn(Optional.of(logEntity));

        BookEntity bookEntity = new BookEntity();
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity));

        when(fineRepository.findById(1)).thenReturn(Optional.empty());

        LocalDate currentDate = LocalDate.now();
        LocalDate expectedReturnDate = currentDate.plusDays(5);
        float totalFine = 0.0f;

        // Act
        ResponseEntity<ReturnResponse> response = underTest.returnBook(1, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(currentDate, Objects.requireNonNull(response.getBody()).getActualReturnDate());
        assertEquals(expectedReturnDate, response.getBody().getExpectedReturnDate());
        assertEquals(totalFine, response.getBody().getFine());
        assertEquals(true, bookEntity.getIsAvailable());
        assertNull(bookEntity.getHolder());
        assertEquals(Date.valueOf(currentDate), logEntity.getReturnDate());
        assertEquals(totalFine, logEntity.getFine());
        verify(bookRepository, times(1)).save(bookEntity);
        verify(logRepository, times(1)).save(logEntity);
    }

    @Test
    void testReturnBook_WithNonExistingBook_ReturnsReturnResponseWithoutBookUpdate() {
        // Arrange
        LogEntity logEntity = new LogEntity();
        logEntity.setMemberId(1);
        logEntity.setBookId(1);
        LocalDate currentDate = LocalDate.now();
        LocalDate issueDate = currentDate.minusDays(5);
        logEntity.setIssueDate(Date.valueOf(issueDate));
        logEntity.setReturnDate(null);
        logEntity.setFine(null);
        when(logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(1, 1)).thenReturn(Optional.of(logEntity));
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        FineEntity fineEntity = new FineEntity(1, 3, 10.0f);
        when(fineRepository.findById(1)).thenReturn(Optional.of(fineEntity));

        LocalDate expectedReturnDate = currentDate.minusDays(5).plusDays(fineEntity.getDaysOverdue());
        float totalFine = fineEntity.getFineAmount() * 2; // Assuming 2 days overdue

        // Act
        ResponseEntity<ReturnResponse> response = underTest.returnBook(1, 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(currentDate, Objects.requireNonNull(response.getBody()).getActualReturnDate());
        assertEquals(expectedReturnDate, response.getBody().getExpectedReturnDate());
        assertEquals(totalFine, response.getBody().getFine());
        verify(bookRepository, never()).save(any());
        verify(logRepository, times(1)).save(logEntity);
    }

    @Test
    void testReturnBook_ExceptionThrown_ReturnsInternalServerError() {
        // Arrange
        when(logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(1, 1)).thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<ReturnResponse> response = underTest.returnBook(1, 1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

}