package com.hacker.boooks.service.impl;

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
import com.hacker.boooks.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Book issue management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class IssueServiceImpl implements IssueService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final FineRepository fineRepository;

    /**
     * Issues a book to a member and returns the expected return date.
     *
     * @param bookId   The ID of the book to be issued.
     * @param memberId The ID of the member to whom the book is issued.
     * @return ResponseEntity containing the issue response with the expected return date.
     */
    @Override
    public ResponseEntity<IssueResponse> issueBook(int bookId, int memberId) {

        try {
            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);

            if (optionalBookEntity.isEmpty() || optionalMemberEntity.isEmpty()) {
                log.debug("BookEntity or MemberEntity not found for bookId {} or memberId {}", bookId, memberId);
                return ResponseEntity.notFound().build();
            }

            BookEntity bookEntity = optionalBookEntity.get();
            bookEntity.setIsAvailable(false);
            bookEntity.setHolder(memberId);
            bookRepository.save(bookEntity);

            LogEntity logEntity = new LogEntity();
            logEntity.setBookId(bookId);
            logEntity.setMemberId(memberId);
            logRepository.save(logEntity);

            Optional<FineEntity> optionalFineEntity = fineRepository.findById(1);
            LocalDate expectedReturnDate = optionalFineEntity
                    .map(fineEntity -> LocalDate.now().plusDays(fineEntity.getDaysOverdue()))
                    .orElseGet(() -> LocalDate.now().plusDays(5));

            log.debug("Book issued successfully for memberId {} and bookId {}", memberId, bookId);

            return ResponseEntity.ok(new IssueResponse(expectedReturnDate));
        } catch (Exception e) {
            log.error("Error occurred while issuing the book for memberId {} and bookId {}: {}", memberId, bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns a book and calculates the fine (if any) for the given book and member.
     *
     * @param bookId   The ID of the book to be returned.
     * @param memberId The ID of the member returning the book.
     * @return ResponseEntity containing the return response with details such as return date, expected return date, and total fine (if applicable).
     */
    @Override
    public ResponseEntity<ReturnResponse> returnBook(int bookId, int memberId) {

        try {
            Optional<LogEntity> optionalLogEntity = logRepository.findByMemberIdAndBookIdAndReturnDateIsNull(memberId, bookId);

            if (optionalLogEntity.isEmpty()) {
                log.debug("LogEntity not found for memberId {} and bookId {}", memberId, bookId);
                return ResponseEntity.notFound().build();
            }

            LogEntity logEntity = optionalLogEntity.get();

            Optional<BookEntity> optionalBookEntity = bookRepository.findById(bookId);

            if (optionalBookEntity.isPresent()) {
                BookEntity bookEntity = optionalBookEntity.get();
                bookEntity.setIsAvailable(true);
                bookEntity.setHolder(null);
                bookRepository.save(bookEntity);
            }

            LocalDate currentDate = LocalDate.now();
            Optional<FineEntity> optionalFineEntity = fineRepository.findById(1);

            LocalDate expectedReturnDate;
            float finePerDay;
            float totalFine = 0.0f;

            if (optionalFineEntity.isPresent()) {
                expectedReturnDate = logEntity.getIssueDate().toLocalDate().plusDays(optionalFineEntity.get().getDaysOverdue());
                finePerDay = optionalFineEntity.get().getFineAmount();
            } else {
                expectedReturnDate = currentDate.plusDays(5);
                finePerDay = 0.0f;
            }

            if (currentDate.isAfter(expectedReturnDate)) {
                long daysDifference = ChronoUnit.DAYS.between(expectedReturnDate, currentDate);
                totalFine = finePerDay * daysDifference;
            }

            logEntity.setReturnDate(Date.valueOf(currentDate));
            logEntity.setFine(totalFine);
            logRepository.save(logEntity);

            log.debug("Book returned successfully for memberId {} and bookId {}", memberId, bookId);

            return ResponseEntity.ok(new ReturnResponse(currentDate, expectedReturnDate, totalFine));
        } catch (Exception e) {
            log.error("Error occurred while returning the book for memberId {} and bookId {}: {}", memberId, bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
