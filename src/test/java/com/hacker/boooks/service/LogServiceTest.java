package com.hacker.boooks.service;

import com.hacker.boooks.bean.Log;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.service.impl.LogServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogServiceImpl underTest;

    @Test
    void getLogs_ReturnLogs_Successfully() {
        // Arrange
        List<LogEntity> logEntities = List.of(
                new LogEntity(1, 1, 1, Date.valueOf("2023-05-01"), null, null),
                new LogEntity(2, 2, 2, Date.valueOf("2023-05-05"), Date.valueOf("2023-05-10"), 10.0f)
        );

        when(logRepository.findAll()).thenReturn(logEntities);

        // Act
        ResponseEntity<List<Log>> response = underTest.getLogs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Log> logs = response.getBody();
        assertNotNull(logs);
        assertEquals(2, logs.size());

        Log log1 = logs.get(0);
        assertEquals(1, log1.getLogId());
        assertEquals(1, log1.getBookId());
        assertEquals(1, log1.getMemberId());
        assertEquals(LocalDate.of(2023, 5, 1), log1.getIssueDate());
        assertNull(log1.getReturnDate());
        assertEquals(0.0f, log1.getFine());

        Log log2 = logs.get(1);
        assertEquals(2, log2.getLogId());
        assertEquals(2, log2.getBookId());
        assertEquals(2, log2.getMemberId());
        assertEquals(LocalDate.of(2023, 5, 5), log2.getIssueDate());
        assertEquals(LocalDate.of(2023, 5, 10), log2.getReturnDate());
        assertEquals(10.0f, log2.getFine());

        verify(logRepository).findAll();
    }

    @Test
    void getLogs_NoLogsFound_ReturnNotFound() {
        // Arrange
        List<LogEntity> logEntities = new ArrayList<>();

        when(logRepository.findAll()).thenReturn(logEntities);

        // Act
        ResponseEntity<List<Log>> response = underTest.getLogs();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(logRepository).findAll();
    }

    @Test
    void getLogs_ExceptionThrown_ReturnInternalServerError() {
        // Arrange
        when(logRepository.findAll()).thenThrow(new RuntimeException("Something went wrong"));

        // Act
        ResponseEntity<List<Log>> response = underTest.getLogs();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(logRepository).findAll();
    }

}