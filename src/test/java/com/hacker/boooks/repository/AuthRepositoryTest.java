package com.hacker.boooks.repository;

import com.hacker.boooks.entity.AuthEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthRepositoryTest {

    private static final String id = "john_doe";

    @Autowired
    @SuppressWarnings("unused")
    private AuthRepository underTest;

    @BeforeEach
    void setUp() {

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(id);
        authEntity.setPassword("P@ssw0rd");
        authEntity.setCreationTime(Timestamp.valueOf("2021-01-01 12:00:00"));
        authEntity.setLastLogin(Timestamp.valueOf("2023-06-05 10:30:00"));
        authEntity.setRefreshToken("abc123def456");
        authEntity.setResetToken("xyz789uvw123");
        authEntity.setIsActivated(true);

        underTest.save(authEntity);

    }

    @Test
    void findAll_WhenAuthExist_ShouldReturnListOfAuths() {
        // Act
        List<AuthEntity> response = underTest.findAll();

        // Assert
        assertThat(response).hasSize(1);
    }

    @Test
    void findAll_WhenNoAuthsExist_ShouldReturnEmptyList() {
        // Arrange
        underTest.deleteAll();

        // Act
        List<AuthEntity> response = underTest.findAll();

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void findById_WhenAuthExists_ShouldReturnAuthById() {
        // Act
        Optional<AuthEntity> response = underTest.findById(id);

        // Assert
        assertThat(response).isNotEmpty();
    }

    @Test
    void findById_WhenAuthDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        String nonExistingId = "unknown";

        // Act
        Optional<AuthEntity> response = underTest.findById(nonExistingId);

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void save_WhenAuthIsNew_ShouldSaveBook() {
        // Arrange
        AuthEntity auth = new AuthEntity();
        auth.setUsername("jane_smith");
        auth.setPassword("StrongPassword123!");
        auth.setCreationTime(Timestamp.valueOf("2022-05-15 18:30:00"));
        auth.setLastLogin(Timestamp.valueOf("2023-06-04 09:15:00"));
        auth.setRefreshToken("abcdef123456");
        auth.setResetToken("xyz789uvw123");
        auth.setIsActivated(true);

        // Act
        AuthEntity savedAuth = underTest.save(auth);

        // Assert
        assertThat(savedAuth.getUsername()).isEqualTo(auth.getUsername());
        assertThat(savedAuth.getPassword()).isEqualTo(auth.getPassword());
        assertThat(savedAuth.getCreationTime()).isEqualTo(auth.getCreationTime());
        assertThat(savedAuth.getLastLogin()).isEqualTo(auth.getLastLogin());
        assertThat(savedAuth.getRefreshToken()).isEqualTo(auth.getRefreshToken());
        assertThat(savedAuth.getResetToken()).isEqualTo(auth.getResetToken());
        assertThat(savedAuth.getIsActivated()).isEqualTo(auth.getIsActivated());

    }

    @Test
    void save_WhenAuthAlreadyExists_ShouldUpdateAuth() {
        // Arrange
        AuthEntity existingAuth = new AuthEntity();
        existingAuth.setUsername("jane_smith");
        existingAuth.setPassword("StrongPassword123!");
        existingAuth.setCreationTime(Timestamp.valueOf("2022-05-15 18:30:00"));
        existingAuth.setLastLogin(Timestamp.valueOf("2023-06-04 09:15:00"));
        existingAuth.setRefreshToken("abcdef123456");
        existingAuth.setResetToken("xyz789uvw123");
        existingAuth.setIsActivated(true);

        existingAuth.setPassword("NewPassword123!");

        // Act
        AuthEntity updatedAuth = underTest.save(existingAuth);

        // Assert
        assertThat(updatedAuth.getPassword()).isEqualTo(existingAuth.getPassword());
    }

    @Test
    void findAllByIsActivatedFalse_WhenAuthExist_ShouldReturnListOfAuths() {
        // Arrange
        AuthEntity auth = new AuthEntity();
        auth.setUsername("jane_smith");
        auth.setPassword("StrongPassword123!");
        auth.setCreationTime(Timestamp.valueOf("2022-05-15 18:30:00"));
        auth.setLastLogin(Timestamp.valueOf("2023-06-04 09:15:00"));
        auth.setRefreshToken("abcdef123456");
        auth.setResetToken("xyz789uvw123");
        auth.setIsActivated(false);
        underTest.save(auth);

        // Act
        List<AuthEntity> response = underTest.findAllByIsActivatedFalse();

        // Assert
        assertThat(response).hasSize(1);
    }

    @Test
    void findAllByIsActivatedFalse_WhenNoAuthsExist_ShouldReturnEmptyList() {

        // Act
        List<AuthEntity> response = underTest.findAllByIsActivatedFalse();

        // Assert
        assertThat(response).isEmpty();
    }

}