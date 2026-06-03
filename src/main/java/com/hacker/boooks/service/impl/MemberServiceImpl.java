package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.Member;
import com.hacker.boooks.bean.MemberBO;
import com.hacker.boooks.bean.MemberProfile;
import com.hacker.boooks.entity.*;
import com.hacker.boooks.repository.*;
import com.hacker.boooks.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    public ResponseEntity<List<Member>> getMembers() {
        try {
            List<MemberEntity> memberEntities = memberRepository.findAll();
            List<Member> members = memberEntities.stream().map(m -> new Member(m.getMemberId(), m.getName(), m.getEmail(), m.getPhoneNumber())).collect(Collectors.toList());
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Member> getMember(int memberId) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);
            if (optionalMemberEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            MemberEntity m = optionalMemberEntity.get();
            return ResponseEntity.ok(new Member(m.getMemberId(), m.getName(), m.getEmail(), m.getPhoneNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<MemberProfile> getMemberProfile(int memberId) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);
            if (optionalMemberEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            MemberEntity m = optionalMemberEntity.get();
            MemberProfile memberProfile = new MemberProfile();
            memberProfile.setName(m.getName());
            memberProfile.setEmail(m.getEmail());
            memberProfile.setPhoneNumber(m.getPhoneNumber());

            List<LogEntity> logEntities = logRepository.findByMemberIdAndReturnDateIsNull(memberId);
            List<Book> currentlyHoldingBooks = new ArrayList<>();
            for (LogEntity logEntity : logEntities) {
                Optional<BookCopyEntity> optionalCopy = bookCopyRepository.findById(logEntity.getCopyId());
                if (optionalCopy.isPresent()) {
                    Optional<BookEntity> optionalBook = bookRepository.findById(optionalCopy.get().getBookId());
                    if (optionalBook.isPresent()) {
                        BookEntity bookEntity = optionalBook.get();
                        Book book = new Book();
                        book.setBookId(bookEntity.getBookId());
                        book.setTitle(bookEntity.getTitle());
                        String authors = bookEntity.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
                        book.setAuthor(authors);
                        currentlyHoldingBooks.add(book);
                    }
                }
            }
            memberProfile.setCurrentlyHolding(currentlyHoldingBooks);
            return ResponseEntity.ok(memberProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> addMember(MemberBO memberBO) {
        try {
            MemberEntity m = new MemberEntity(null, memberBO.getName(), memberBO.getEmail(), memberBO.getPhoneNumber());
            memberRepository.save(m);
            return ResponseEntity.ok("Member added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> updateMember(int memberId, MemberBO memberBO) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);
            if (optionalMemberEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            MemberEntity m = optionalMemberEntity.get();
            m.setName(memberBO.getName());
            m.setEmail(memberBO.getEmail());
            m.setPhoneNumber(memberBO.getPhoneNumber());
            memberRepository.save(m);
            return ResponseEntity.ok("Member updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteMember(int memberId) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(memberId);
            if (optionalMemberEntity.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            memberRepository.delete(optionalMemberEntity.get());
            return ResponseEntity.ok("Member deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Book>> getBooksForMember(int memberId) {
        try {
            List<LogEntity> logEntities = logRepository.findByMemberIdAndReturnDateIsNull(memberId);
            List<Book> currentlyHoldingBooks = new ArrayList<>();
            for (LogEntity logEntity : logEntities) {
                Optional<BookCopyEntity> optionalCopy = bookCopyRepository.findById(logEntity.getCopyId());
                if (optionalCopy.isPresent()) {
                    Optional<BookEntity> optionalBook = bookRepository.findById(optionalCopy.get().getBookId());
                    if (optionalBook.isPresent()) {
                        BookEntity bookEntity = optionalBook.get();
                        Book book = new Book();
                        book.setBookId(bookEntity.getBookId());
                        book.setTitle(bookEntity.getTitle());
                        String authors = bookEntity.getAuthors().stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
                        book.setAuthor(authors);
                        currentlyHoldingBooks.add(book);
                    }
                }
            }
            return ResponseEntity.ok(currentlyHoldingBooks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}