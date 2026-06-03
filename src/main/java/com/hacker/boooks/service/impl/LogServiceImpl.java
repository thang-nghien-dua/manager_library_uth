package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.Log;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Log management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;

    /**
     * Retrieves a list of all book logs.
     *
     * @return ResponseEntity with a list of book logs if available, or an empty list if no logs exist.
     */
    public ResponseEntity<List<Log>> getLogs() {

        try {
            List<LogEntity> logEntities = logRepository.findAll();

            if (logEntities.isEmpty()) {
                log.debug("No logs found.");
                return ResponseEntity.notFound().build();
            }

            List<Log> logs = new ArrayList<>();

            for (LogEntity logEntity : logEntities) {
                Log bookLog = new Log();
                bookLog.setLogId(logEntity.getLogId());
                bookLog.setBookId(logEntity.getBookId());
                bookLog.setMemberId(logEntity.getMemberId());
                bookLog.setIssueDate(logEntity.getIssueDate().toLocalDate());
                bookLog.setReturnDate(logEntity.getReturnDate() != null ? logEntity.getReturnDate().toLocalDate() : null);
                bookLog.setFine(logEntity.getFine() != null ? logEntity.getFine() : 0.0f);

                logs.add(bookLog);
            }

            log.info("Retrieved {} book logs", logs.size());

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error occurred while retrieving book logs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
