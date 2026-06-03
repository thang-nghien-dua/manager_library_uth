package com.hacker.boooks.repository;

import com.hacker.boooks.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    private static final int memberId = 1;
    private static final String name = "John Doe";
    private static final String email = "john@example.com";
    private static final String phoneNumber = "1234567890";

    @Autowired
    @SuppressWarnings("unused")
    private MemberRepository underTest;

    @BeforeEach
    void setUp() {
        MemberEntity member = new MemberEntity();
        member.setMemberId(memberId);
        member.setName(name);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);

        underTest.save(member);

    }

    @Test
    void findAll_WhenMembersExist_ShouldReturnListOfMembers() {
        // Act
        List<MemberEntity> members = underTest.findAll();

        // Assert
        assertThat(members).isNotEmpty().hasSize(1);
    }

    @Test
    void findAll_WhenNoMembersExist_ShouldReturnEmptyList() {
        // Arrange
        underTest.deleteAll();

        // Act
        List<MemberEntity> members = underTest.findAll();

        // Assert
        assertThat(members).isEmpty();
    }

    @Test
    void findById_WhenMemberExists_ShouldReturnMemberById() {
        // Act
        Optional<MemberEntity> optionalMember = underTest.findById(memberId);

        // Assert
        assertThat(optionalMember).isPresent();
        assertThat(optionalMember.get().getMemberId()).isEqualTo(memberId);
    }

    @Test
    void findById_WhenMemberDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        int memberId = -1;

        // Act
        Optional<MemberEntity> optionalMember = underTest.findById(memberId);

        // Assert
        assertThat(optionalMember).isEmpty();
    }

    @Test
    void save_WhenMemberIsSaved_ShouldReturnSavedMember() {
        // Arrange
        MemberEntity member = new MemberEntity();
        member.setMemberId(2);
        member.setName("Jane Smith");
        member.setEmail("jane@example.com");
        member.setPhoneNumber("9876543210");

        // Act
        MemberEntity savedMember = underTest.save(member);

        // Assert
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(savedMember.getName()).isEqualTo(member.getName());
        assertThat(savedMember.getEmail()).isEqualTo(member.getEmail());
        assertThat(savedMember.getPhoneNumber()).isEqualTo(member.getPhoneNumber());
    }

    @Test
    void findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase_WhenMembersExist_ShouldReturnMatchingMembers() {
        // Act
        List<MemberEntity> matchingMembers = underTest.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase("John", "example", "7890");

        // Assert
        assertThat(matchingMembers).isNotEmpty().hasSize(1);
    }

    @Test
    void findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase_WhenNoMatchingMembersExist_ShouldReturnEmptyList() {
        // Act
        List<MemberEntity> matchingMembers = underTest.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase("Jane", "email", "9876");

        // Assert
        assertThat(matchingMembers).isEmpty();
    }

}