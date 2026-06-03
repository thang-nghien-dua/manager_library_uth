package com.hacker.boooks.service;

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
import com.hacker.boooks.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LogRepository logRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private MemberServiceImpl underTest;

    @Test
    void getMembers_ReturnMembers_Successfully() {
        // Arrange
        List<MemberEntity> memberEntities = List.of(
                new MemberEntity(1, "John Doe", "john@example.com", "1234567890"),
                new MemberEntity(2, "Jane Smith", "jane@example.com", "9876543210")
        );

        when(memberRepository.findAll()).thenReturn(memberEntities);

        // Act
        ResponseEntity<List<Member>> response = underTest.getMembers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Member> members = response.getBody();
        assertNotNull(members);
        assertEquals(2, members.size());

        Member member1 = members.get(0);
        assertEquals(1, member1.getMemberId());
        assertEquals("John Doe", member1.getName());
        assertEquals("john@example.com", member1.getEmail());
        assertEquals("1234567890", member1.getPhoneNumber());

        Member member2 = members.get(1);
        assertEquals(2, member2.getMemberId());
        assertEquals("Jane Smith", member2.getName());
        assertEquals("jane@example.com", member2.getEmail());
        assertEquals("9876543210", member2.getPhoneNumber());

        verify(memberRepository).findAll();
    }

    @Test
    void getMembers_NoMembersFound_ReturnNotFound() {
        // Arrange
        List<MemberEntity> memberEntities = new ArrayList<>();
        when(memberRepository.findAll()).thenReturn(memberEntities);

        // Act
        ResponseEntity<List<Member>> response = underTest.getMembers();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository).findAll();
    }

    @Test
    void getMembers_ExceptionThrown_ReturnInternalServerError() {
        // Arrange
        when(memberRepository.findAll()).thenThrow(new RuntimeException("Something went wrong"));

        // Act
        ResponseEntity<List<Member>> response = underTest.getMembers();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(memberRepository).findAll();
    }

    @Test
    void getMember_ExistingMemberId_ReturnMember() {
        // Arrange
        int memberId = 1;
        MemberEntity memberEntity = new MemberEntity(1, "John Doe", "john@example.com", "1234567890");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberEntity));

        // Act
        ResponseEntity<Member> response = underTest.getMember(memberId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Member member = response.getBody();
        assertNotNull(member);
        assertEquals(memberId, member.getMemberId());
        assertEquals("John Doe", member.getName());
        assertEquals("john@example.com", member.getEmail());
        assertEquals("1234567890", member.getPhoneNumber());

        verify(memberRepository).findById(memberId);
    }

    @Test
    void getMember_NonExistingMemberId_ReturnNotFound() {
        // Arrange
        int memberId = 1;
        Optional<MemberEntity> optionalMemberEntity = Optional.empty();
        when(memberRepository.findById(memberId)).thenReturn(optionalMemberEntity);

        // Act
        ResponseEntity<Member> response = underTest.getMember(memberId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository).findById(memberId);
    }

    @Test
    void getMember_ExceptionThrown_ReturnInternalServerError() {
        // Arrange
        int memberId = 1;
        when(memberRepository.findById(memberId)).thenThrow(new RuntimeException("Something went wrong"));

        // Act
        ResponseEntity<Member> response = underTest.getMember(memberId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(memberRepository).findById(memberId);
    }

    @Test
    void getMemberProfile_ExistingMemberId_ReturnMemberProfile() {
        // Arrange
        int memberId = 1;
        MemberEntity memberEntity = new MemberEntity(memberId, "John Doe", "john@example.com", "1234567890");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberEntity));

        List<LogEntity> logEntities = new ArrayList<>();
        LogEntity logEntity = new LogEntity(1, 1, 1, Date.valueOf("2023-05-01"), null, null);
        logEntities.add(logEntity);
        when(logRepository.findByMemberIdAndReturnDateIsNull(memberId)).thenReturn(logEntities);

        BookEntity bookEntity = new BookEntity(1, "Book Title", "Book Author", "Book Genre", Date.valueOf(LocalDate.now()), false, memberId);
        when(bookRepository.findById(logEntity.getBookId())).thenReturn(Optional.of(bookEntity));

        // Act
        ResponseEntity<MemberProfile> response = underTest.getMemberProfile(memberId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MemberProfile memberProfile = response.getBody();
        assertNotNull(memberProfile);
        assertEquals("John Doe", memberProfile.getName());
        assertEquals("john@example.com", memberProfile.getEmail());
        assertEquals("1234567890", memberProfile.getPhoneNumber());
        assertEquals(1, memberProfile.getCurrentlyHolding().size());

        verify(memberRepository).findById(memberId);
        verify(logRepository).findByMemberIdAndReturnDateIsNull(memberId);
        verify(bookRepository).findById(logEntity.getBookId());
    }

    @Test
    void getMemberProfile_NonExistingMemberId_ReturnNotFound() {
        // Arrange
        int memberId = 1;
        Optional<MemberEntity> optionalMemberEntity = Optional.empty();
        when(memberRepository.findById(memberId)).thenReturn(optionalMemberEntity);

        // Act
        ResponseEntity<MemberProfile> response = underTest.getMemberProfile(memberId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository).findById(memberId);
        verifyNoInteractions(logRepository, bookRepository);
    }

    @Test
    void getMemberProfile_NoCurrentlyHoldingBooks_ReturnNotFound() {
        // Arrange
        int memberId = 1;
        MemberEntity memberEntity = new MemberEntity(memberId, "John Doe", "john@example.com", "1234567890");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberEntity));
        List<LogEntity> optionalLogEntities = new ArrayList<>();
        when(logRepository.findByMemberIdAndReturnDateIsNull(memberId)).thenReturn(optionalLogEntities);

        // Act
        ResponseEntity<MemberProfile> response = underTest.getMemberProfile(memberId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository).findById(memberId);
        verify(logRepository).findByMemberIdAndReturnDateIsNull(memberId);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void addMember_ValidMemberBO_ReturnsOkResponse() {
        // Arrange
        MemberBO memberBO = new MemberBO();
        memberBO.setName("John Doe");
        memberBO.setEmail("john.doe@example.com");
        memberBO.setPhoneNumber("1234567890");

        // Act
        ResponseEntity<String> response = underTest.addMember(memberBO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Member added successfully", response.getBody());

        // Assert
        ArgumentCaptor<MemberEntity> memberEntityCaptor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository, times(1)).save(memberEntityCaptor.capture());
        MemberEntity savedMemberEntity = memberEntityCaptor.getValue();
        assertEquals(memberBO.getName(), savedMemberEntity.getName());
        assertEquals(memberBO.getEmail(), savedMemberEntity.getEmail());
        assertEquals(memberBO.getPhoneNumber(), savedMemberEntity.getPhoneNumber());
    }

    @Test
    void addMember_ExceptionThrown_ReturnsInternalServerErrorResponse() {
        // Arrange
        MemberBO memberBO = new MemberBO();
        memberBO.setName("John Doe");
        memberBO.setEmail("john.doe@example.com");
        memberBO.setPhoneNumber("1234567890");

        doThrow(new RuntimeException("Something went wrong")).when(memberRepository).save(any(MemberEntity.class));

        // Act
        ResponseEntity<String> response = underTest.addMember(memberBO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }

    @Test
    void updateMember_ValidMemberIdAndMemberBO_ReturnsOkResponse() {
        // Arrange
        int memberId = 1;
        MemberBO memberBO = new MemberBO();
        memberBO.setName("John Doe");
        memberBO.setEmail("john.doe@example.com");
        memberBO.setPhoneNumber("1234567890");

        MemberEntity existingMemberEntity = new MemberEntity();
        existingMemberEntity.setMemberId(memberId);
        existingMemberEntity.setName("Jane Smith");
        existingMemberEntity.setEmail("jane.smith@example.com");
        existingMemberEntity.setPhoneNumber("0987654321");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMemberEntity));

        // Act
        ResponseEntity<String> response = underTest.updateMember(memberId, memberBO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Member updated successfully", response.getBody());

        ArgumentCaptor<MemberEntity> memberEntityCaptor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository, times(1)).save(memberEntityCaptor.capture());
        MemberEntity savedMemberEntity = memberEntityCaptor.getValue();
        assertEquals(memberId, savedMemberEntity.getMemberId());
        assertEquals(memberBO.getName(), savedMemberEntity.getName());
        assertEquals(memberBO.getEmail(), savedMemberEntity.getEmail());
        assertEquals(memberBO.getPhoneNumber(), savedMemberEntity.getPhoneNumber());
    }

    @Test
    void updateMember_MemberNotFound_ReturnsNotFoundResponse() {
        // Arrange
        int memberId = 1;
        MemberBO memberBO = new MemberBO();
        memberBO.setName("John Doe");
        memberBO.setEmail("john.doe@example.com");
        memberBO.setPhoneNumber("1234567890");
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.updateMember(memberId, memberBO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository, never()).save(any(MemberEntity.class));
    }

    @Test
    void updateMember_ExceptionThrown_ReturnsInternalServerErrorResponse() {
        // Arrange
        int memberId = 1;
        MemberBO memberBO = new MemberBO();
        memberBO.setName("John Doe");
        memberBO.setEmail("john.doe@example.com");
        memberBO.setPhoneNumber("1234567890");

        MemberEntity existingMemberEntity = new MemberEntity();
        existingMemberEntity.setMemberId(memberId);
        existingMemberEntity.setName("Jane Smith");
        existingMemberEntity.setEmail("jane.smith@example.com");
        existingMemberEntity.setPhoneNumber("0987654321");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMemberEntity));
        when(memberRepository.save(any(MemberEntity.class))).thenThrow(new RuntimeException("Some error occurred"));

        // Act
        ResponseEntity<String> response = underTest.updateMember(memberId, memberBO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ArgumentCaptor<MemberEntity> memberEntityCaptor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository, times(1)).save(memberEntityCaptor.capture());
        MemberEntity savedMemberEntity = memberEntityCaptor.getValue();
        assertEquals(memberId, savedMemberEntity.getMemberId());
        assertEquals(memberBO.getName(), savedMemberEntity.getName());
        assertEquals(memberBO.getEmail(), savedMemberEntity.getEmail());
        assertEquals(memberBO.getPhoneNumber(), savedMemberEntity.getPhoneNumber());
    }

    @Test
    void deleteMember_ValidMemberId_ReturnsOkResponse() {
        // Arrange
        int memberId = 1;
        MemberEntity existingMemberEntity = new MemberEntity();
        existingMemberEntity.setMemberId(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMemberEntity));

        // Act
        ResponseEntity<String> response = underTest.deleteMember(memberId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Member deleted successfully", response.getBody());

        verify(memberRepository, times(1)).findById(memberId);

        ArgumentCaptor<MemberEntity> memberEntityCaptor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository, times(1)).delete(memberEntityCaptor.capture());
        MemberEntity deletedMemberEntity = memberEntityCaptor.getValue();
        assertEquals(memberId, deletedMemberEntity.getMemberId());
    }

    @Test
    void deleteMember_MemberNotFound_ReturnsNotFoundResponse() {
        // Arrange
        int memberId = 1;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.deleteMember(memberId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void deleteMember_ExceptionThrown_ReturnsInternalServerErrorResponse() {
        // Arrange
        int memberId = 1;
        when(memberRepository.findById(memberId)).thenThrow(new RuntimeException("Some error occurred"));

        // Act
        ResponseEntity<String> response = underTest.deleteMember(memberId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void getBooksForMember_ValidMemberId_ReturnsListOfBooks() {
        // Arrange
        int memberId = 1;
        LogEntity logEntity1 = new LogEntity(1, 1, memberId, Date.valueOf("2023-05-01"), null, null);
        LogEntity logEntity2 = new LogEntity(2, 2, memberId, Date.valueOf("2023-05-05"), Date.valueOf("2023-05-10"), 10.0f);

        List<LogEntity> logEntities = new ArrayList<>();
        logEntities.add(logEntity1);
        logEntities.add(logEntity2);
        when(logRepository.findByMemberIdAndReturnDateIsNull(memberId)).thenReturn(logEntities);

        BookEntity bookEntity1 = new BookEntity(1, "Book 1", "Author 1", "Genre 1", Date.valueOf(LocalDate.now()), true, null);
        BookEntity bookEntity2 = new BookEntity(2, "Book 2", "Author 1", "Genre 2", Date.valueOf(LocalDate.now()), false, 1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(bookEntity1));
        when(bookRepository.findById(2)).thenReturn(Optional.of(bookEntity2));

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooksForMember(memberId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Book> books = response.getBody();
        assertNotNull(books);
        assertEquals(2, books.size());

        verify(logRepository, times(1)).findByMemberIdAndReturnDateIsNull(memberId);
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).findById(2);
    }

    @Test
    void getBooksForMember_MemberHasNoBooks_ReturnsNotFoundResponse() {
        // Arrange
        int memberId = 1;

        when(logRepository.findByMemberIdAndReturnDateIsNull(memberId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooksForMember(memberId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(logRepository, times(1)).findByMemberIdAndReturnDateIsNull(memberId);
    }

    @Test
    void getBooksForMember_ExceptionThrown_ReturnsInternalServerErrorResponse() {
        // Arrange
        int memberId = 1;
        when(logRepository.findByMemberIdAndReturnDateIsNull(memberId)).thenThrow(new RuntimeException("Some error occurred"));

        // Act
        ResponseEntity<List<Book>> response = underTest.getBooksForMember(memberId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(logRepository, times(1)).findByMemberIdAndReturnDateIsNull(memberId);
    }

}
