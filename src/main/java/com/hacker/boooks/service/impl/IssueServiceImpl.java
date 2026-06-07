package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.IssueResponse;
import com.hacker.boooks.bean.ReturnResponse;
import com.hacker.boooks.entity.*;
import com.hacker.boooks.repository.*;
import com.hacker.boooks.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueServiceImpl implements IssueService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final FineRepository fineRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    @Transactional
    public ResponseEntity<IssueResponse> issueBook(int bookId, int memberId, String dueDateStr) {
        try {
            Optional<BookEntity> bookOpt = bookRepository.findById(bookId);
            Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
            if (bookOpt.isEmpty() || memberOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Find an available book copy
            List<BookCopyEntity> copies = bookCopyRepository.findByBookIdAndStatus(bookId, BookCopyEntity.Status.AVAILABLE);
            if (copies.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            BookCopyEntity copy = copies.get(0);
            copy.setStatus(BookCopyEntity.Status.BORROWED);
            bookCopyRepository.save(copy);

            LogEntity logEntity = new LogEntity();
            logEntity.setCopyId(copy.getCopyId());
            logEntity.setMemberId(memberId);
            logEntity.setIssueDate(Date.valueOf(LocalDate.now()));
            
            LocalDate expectedReturnDate = LocalDate.parse(dueDateStr);
            logEntity.setDueDate(Date.valueOf(expectedReturnDate));
            logRepository.save(logEntity);

            return ResponseEntity.ok(new IssueResponse(expectedReturnDate));
        } catch (Exception e) {
            log.error("Error issuing book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ReturnResponse> returnBook(int bookId, int memberId) {
        try {
            // Find a copy of the book currently borrowed by the member
            List<BookCopyEntity> borrowedCopies = bookCopyRepository.findByBookId(bookId);
            Optional<LogEntity> activeLogOpt = Optional.empty();
            BookCopyEntity copy = null;

            for (BookCopyEntity c : borrowedCopies) {
                Optional<LogEntity> logOpt = logRepository.findByMemberIdAndCopyIdAndReturnDateIsNull(memberId, c.getCopyId());
                if (logOpt.isPresent()) {
                    activeLogOpt = logOpt;
                    copy = c;
                    break;
                }
            }

            if (activeLogOpt.isEmpty() || copy == null) {
                return ResponseEntity.notFound().build();
            }

            LogEntity logEntity = activeLogOpt.get();
            copy.setStatus(BookCopyEntity.Status.AVAILABLE);
            bookCopyRepository.save(copy);

            LocalDate currentDate = LocalDate.now();
            
            LocalDate expectedReturnDate = logEntity.getDueDate() != null ? logEntity.getDueDate().toLocalDate() : logEntity.getIssueDate().toLocalDate().plusDays(14);
            float finePerDay = 1000.0f; // As requested: quá 1 ngày là 1k
            float totalFine = 0.0f;

            if (currentDate.isAfter(expectedReturnDate)) {
                long daysDifference = ChronoUnit.DAYS.between(expectedReturnDate, currentDate);
                totalFine = finePerDay * daysDifference;
            }

            logEntity.setReturnDate(Date.valueOf(currentDate));
            logEntity.setFine(totalFine);
            logRepository.save(logEntity);

            return ResponseEntity.ok(new ReturnResponse(currentDate, expectedReturnDate, totalFine));
        } catch (Exception e) {
            log.error("Error returning book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}