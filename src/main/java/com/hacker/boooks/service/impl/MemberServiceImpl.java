package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.Member;
import com.hacker.boooks.bean.MemberBO;
import com.hacker.boooks.bean.MemberProfile;
import com.hacker.boooks.entity.BookEntity;
import com.hacker.boooks.entity.LogEntity;
import com.hacker.boooks.entity.MemberEntity;
import com.hacker.boooks.repository.BookRepository;
import com.hacker.boooks.repository.LogRepository;
import com.hacker.boooks.repository.MemberRepository;
import com.hacker.boooks.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Member management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class MemberServiceImpl implements MemberService {

    private static final String MEMBER_NOT_FOUND = "Member with ID {} not found";

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final BookRepository bookRepository;

    /**
     * Get all members.
     *
     * @return ResponseEntity containing a list of all members
     */
    @Override
    public ResponseEntity<List<Member>> getMembers() {

        try {
            List<MemberEntity> memberEntities = memberRepository.findAll();

            if (memberEntities.isEmpty()) {
                log.debug("No members found.");
                return ResponseEntity.notFound().build();
            }

            List<Member> members = new ArrayList<>();

            for (MemberEntity memberEntity : memberEntities) {
                Member member = new Member(memberEntity.getMemberId(), memberEntity.getName(), memberEntity.getEmail(), memberEntity.getPhoneNumber());
                members.add(member);
            }

            log.debug("Retrieved {} members successfully", members.size());
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            log.error("Error occurred while retrieving members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a member by ID.
     *
     * @param memberId the ID of the member to retrieve
     * @return ResponseEntity containing the member information if found, or not found response if not found
     */
    @Override
    public ResponseEntity<Member> getMember(int memberId) {

        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);

            if (optionalMemberEntity.isEmpty()) {
                log.debug(MEMBER_NOT_FOUND, memberId);
                return ResponseEntity.notFound().build();
            }

            MemberEntity memberEntity = optionalMemberEntity.get();
            Member member = new Member(memberEntity.getMemberId(), memberEntity.getName(), memberEntity.getEmail(), memberEntity.getPhoneNumber());

            log.debug("Retrieved member with ID {}: {}", memberId, member);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            log.error("Error occurred while retrieving member with ID {}: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the profile of a member.
     *
     * @param memberId the ID of the member
     * @return ResponseEntity containing the member profile if found, or not found response if not found
     */
    @Override
    public ResponseEntity<MemberProfile> getMemberProfile(int memberId) {

        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);

            if (optionalMemberEntity.isEmpty()) {
                log.debug(MEMBER_NOT_FOUND, memberId);
                return ResponseEntity.notFound().build();
            }

            MemberEntity memberEntity = optionalMemberEntity.get();
            MemberProfile memberProfile = new MemberProfile();
            memberProfile.setName(memberEntity.getName());
            memberProfile.setEmail(memberEntity.getEmail());
            memberProfile.setPhoneNumber(memberEntity.getPhoneNumber());

            List<LogEntity> logEntities = logRepository.findByMemberIdAndReturnDateIsNull(memberId);
            if (logEntities.isEmpty()) {
                log.debug("Member with ID {} doesn't hold any books", memberId);
                return ResponseEntity.notFound().build();
            }
            List<Book> currentlyHoldingBooks = new ArrayList<>();
            for (LogEntity logEntity : logEntities) {
                Optional<BookEntity> optionalBookEntity = bookRepository.findById(logEntity.getBookId());
                if (optionalBookEntity.isPresent()) {
                    BookEntity bookEntity = optionalBookEntity.get();
                    Book book = new Book();
                    book.setBookId(bookEntity.getBookId());
                    book.setTitle(bookEntity.getTitle());
                    book.setAuthor(bookEntity.getAuthor());
                    book.setPublication(bookEntity.getPublication().toLocalDate());
                    book.setAvailable(bookEntity.getIsAvailable());
                    book.setHolder(bookEntity.getHolder());
                    currentlyHoldingBooks.add(book);
                }
            }
            memberProfile.setCurrentlyHolding(currentlyHoldingBooks);
            log.info("Member profile fetched for ID: {}", memberId);
            return ResponseEntity.ok(memberProfile);
        } catch (Exception e) {
            log.error("Error occurred while retrieving member profile for ID {}: {}", memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Adds a new member to the library.
     *
     * @param memberBO The MemberBO object containing the member details to be added.
     * @return ResponseEntity with a success message if the member is added successfully, or an error response if an exception occurs.
     */
    @Override
    public ResponseEntity<String> addMember(MemberBO memberBO) {

        try {
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setName(memberBO.getName());
            memberEntity.setEmail(memberBO.getEmail());
            memberEntity.setPhoneNumber(memberBO.getPhoneNumber());

            memberRepository.save(memberEntity);

            log.info("Member added successfully");

            return ResponseEntity.ok("Member added successfully");
        } catch (Exception e) {
            log.error("Error occurred while adding a member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the details of a member in the library.
     *
     * @param memberId The ID of the member to be updated.
     * @param memberBO The MemberBO object containing the updated member details.
     * @return ResponseEntity with a success message if the member is updated successfully, or a not found response if the member ID is invalid.
     */
    @Override
    public ResponseEntity<String> updateMember(int memberId, MemberBO memberBO) {

        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);

            if (optionalMemberEntity.isEmpty()) {
                log.debug(MEMBER_NOT_FOUND, memberId);
                return ResponseEntity.notFound().build();
            }

            MemberEntity memberEntity = optionalMemberEntity.get();
            memberEntity.setName(memberBO.getName());
            memberEntity.setEmail(memberBO.getEmail());
            memberEntity.setPhoneNumber(memberBO.getPhoneNumber());

            memberRepository.save(memberEntity);

            log.info("Member updated successfully");

            return ResponseEntity.ok("Member updated successfully");
        } catch (Exception e) {
            log.error("Error occurred while updating the member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a member from the library.
     *
     * @param memberId The ID of the member to be deleted.
     * @return ResponseEntity with a success message if the member is deleted successfully, or a not found response if the member ID is invalid.
     */
    @Override
    public ResponseEntity<String> deleteMember(int memberId) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);

            if (optionalMemberEntity.isEmpty()) {
                log.debug(MEMBER_NOT_FOUND, memberId);
                return ResponseEntity.notFound().build();
            }

            MemberEntity memberEntity = optionalMemberEntity.get();
            memberRepository.delete(memberEntity);

            log.info("Member deleted successfully");

            return ResponseEntity.ok("Member deleted successfully");
        } catch (Exception e) {
            log.error("Error occurred while deleting the member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the books currently held by a member.
     *
     * @param memberId the ID of the member
     * @return ResponseEntity containing the list of books currently held by the member
     */
    @Override
    public ResponseEntity<List<Book>> getBooksForMember(int memberId) {
        try {
            List<LogEntity> logEntities = logRepository.findByMemberIdAndReturnDateIsNull(memberId);
            if (logEntities.isEmpty()) {
                log.debug("Member with ID {} doesn't hold any books", memberId);
                return ResponseEntity.notFound().build();
            }

            List<Book> currentlyHoldingBooks = new ArrayList<>();

            for (LogEntity logEntity : logEntities) {
                Optional<BookEntity> optionalBookEntity = bookRepository.findById(logEntity.getBookId());

                if (optionalBookEntity.isPresent()) {
                    BookEntity bookEntity = optionalBookEntity.get();
                    Book book = new Book();
                    book.setBookId(bookEntity.getBookId());
                    book.setTitle(bookEntity.getTitle());
                    book.setAuthor(bookEntity.getAuthor());
                    book.setPublication(bookEntity.getPublication().toLocalDate());
                    book.setAvailable(bookEntity.getIsAvailable());
                    book.setHolder(bookEntity.getHolder());
                    currentlyHoldingBooks.add(book);
                }
            }

            log.info("Number of books currently held by member with ID {}: {}", memberId, currentlyHoldingBooks.size());
            return ResponseEntity.ok(currentlyHoldingBooks);
        } catch (Exception e) {
            log.error("Failed to retrieve books currently held by member with ID {}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
