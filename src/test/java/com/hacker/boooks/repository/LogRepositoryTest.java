package com.hacker.boooks.repository;

import com.hacker.boooks.entity.LogEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LogRepositoryTest {

    private static final int logId1 = 1;
    private static final int bookId1 = 1;
    private static final int memberId1 = 1;
    private static final int logId2 = 2;
    private static final int bookId2 = 2;
    private static final int memberId2 = 2;

    @Autowired
    @SuppressWarnings("unused")
    private LogRepository underTest;

    @BeforeEach
    void setUp() {
        Date issueDate1 = Date.valueOf(LocalDate.now());
        Date returnDate1 = Date.valueOf(LocalDate.now().plusDays(7));
        Float fine1 = 12.5f;

        Date issueDate2 = Date.valueOf(LocalDate.now());
        Float fine2 = 0.0f;

        LogEntity logEntity1 = new LogEntity(logId1, bookId1, memberId1, issueDate1, returnDate1, fine1);
        LogEntity logEntity2 = new LogEntity(logId2, bookId2, memberId2, issueDate2, null, fine2);
        underTest.saveAll(List.of(logEntity1, logEntity2));
    }

    @Test
    void findAll_WhenLogsExist_ShouldReturnListOfLogs() {
        // Act
        List<LogEntity> logs = underTest.findAll();

        // Assert
        assertThat(logs).isNotEmpty().hasSize(2);
    }

    @Test
    void findAll_WhenNoLogsExist_ShouldReturnEmptyList() {
        // Arrange
        underTest.deleteAll();

        // Act
        List<LogEntity> logs = underTest.findAll();

        // Assert
        assertThat(logs).isEmpty();
    }

    @Test
    void findById_WhenLogExists_ShouldReturnLogById() {
        // Act
        Optional<LogEntity> retrievedLog = underTest.findById(logId1);

        // Assert
        assertThat(retrievedLog).isPresent();
    }

    @Test
    void findById_WhenLogDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<LogEntity> retrievedLog = underTest.findById(99999);

        // Assert
        assertThat(retrievedLog).isEmpty();
    }

    @Test
    void save_WhenLogIsNew_ShouldSaveLog() {
        // Arrange
        LogEntity log = new LogEntity();
        log.setLogId(2);
        log.setBookId(123);
        log.setMemberId(456);
        log.setIssueDate(Date.valueOf(LocalDate.now()));
        log.setReturnDate(null);
        log.setFine(0.0f);

        // Act
        LogEntity savedLog = underTest.save(log);

        // Assert
        assertThat(underTest.findById(2)).isPresent();
        assertThat(savedLog.getBookId()).isEqualTo(log.getBookId());
        assertThat(savedLog.getMemberId()).isEqualTo(log.getMemberId());
        assertThat(savedLog.getIssueDate()).isEqualTo(log.getIssueDate());
        assertThat(savedLog.getReturnDate()).isEqualTo(log.getReturnDate());
        assertThat(savedLog.getFine()).isEqualTo(log.getFine());
    }

    @Test
    void save_WhenLogExists_ShouldUpdateLog() {
        // Arrange
        LogEntity log = new LogEntity();
        log.setBookId(123);
        log.setMemberId(456);
        log.setIssueDate(Date.valueOf(LocalDate.now()));
        log.setReturnDate(null);
        log.setFine(0.0f);
        LogEntity savedLog = underTest.save(log);

        // Update the log
        savedLog.setReturnDate(Date.valueOf(LocalDate.now().plusDays(7)));
        savedLog.setFine(10.0f);

        // Act
        LogEntity updatedLog = underTest.save(savedLog);

        // Assert
        assertThat(updatedLog.getReturnDate()).isNotNull();
        assertThat(updatedLog.getFine()).isEqualTo(10.0f);
    }

    @Test
    void findByMemberIdAndReturnDateIsNull_WhenLogsExistForMember_ShouldReturnListOfLogs() {
        // Act
        List<LogEntity> logs = underTest.findByMemberIdAndReturnDateIsNull(memberId2);

        // Assert
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getLogId()).isEqualTo(2);
    }

    @Test
    void findByMemberIdAndReturnDateIsNull_WhenNoLogsExistForMember_ShouldReturnEmptyList() {
        // Act
        List<LogEntity> logs = underTest.findByMemberIdAndReturnDateIsNull(memberId1);

        // Assert
        assertThat(logs).isEmpty();
    }

    @Test
    void findByBookIdIn_WhenLogsExistForBooks_ShouldReturnListOfLogs() {
        // Arrange
        List<Integer> bookIds = List.of(1, 2);

        // Act
        List<LogEntity> logs = underTest.findByBookIdIn(bookIds);

        // Assert
        assertThat(logs).hasSize(2);
    }

    @Test
    void findByBookIdIn_WhenNoLogsExistForBooks_ShouldReturnEmptyList() {
        // Arrange
        List<Integer> bookIds = List.of(123, 456);

        // Act
        List<LogEntity> logs = underTest.findByBookIdIn(bookIds);

        // Assert
        assertThat(logs).isEmpty();
    }

    @Test
    void findByMemberIdAndBookIdAndReturnDateIsNull_WhenLogExistsForMemberAndBook_ShouldReturnLog() {
        // Act
        Optional<LogEntity> retrievedLog = underTest.findByMemberIdAndBookIdAndReturnDateIsNull(memberId2, bookId2);

        // Assert
        assertThat(retrievedLog).isPresent();
    }

    @Test
    void findByMemberIdAndBookIdAndReturnDateIsNull_WhenLogDoesNotExistForMemberAndBook_ShouldReturnEmptyOptional() {
        // Arrange
        int memberId = 456;
        int bookId = 123;

        // Act
        Optional<LogEntity> retrievedLog = underTest.findByMemberIdAndBookIdAndReturnDateIsNull(memberId, bookId);

        // Assert
        assertThat(retrievedLog).isEmpty();
    }
}