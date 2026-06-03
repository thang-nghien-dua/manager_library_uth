package com.hacker.boooks.service;

import com.hacker.boooks.bean.AccessTokenResponse;
import com.hacker.boooks.bean.AuthenticationResponse;
import com.hacker.boooks.bean.Librarian;
import com.hacker.boooks.config.JwtService;
import com.hacker.boooks.entity.AuthEntity;
import com.hacker.boooks.repository.AuthRepository;
import com.hacker.boooks.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private AuthRepository authRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationServiceImpl underTest;

    @Test
    void testRegister_Success() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        String encodedPassword = "encodedpassword";
        String refreshToken = "refreshtoken";
        String accessToken = "accesstoken";

        AuthenticationResponse expectedResponse = new AuthenticationResponse();
        expectedResponse.setAccessToken(accessToken);
        expectedResponse.setRefreshToken(refreshToken);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(AuthEntity.class))).thenReturn(accessToken);
        when(authRepository.save(any(AuthEntity.class))).thenReturn(null);


        // Act
        ResponseEntity<AuthenticationResponse> response = underTest.register(username, password);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse.getAccessToken(), response.getBody().getAccessToken());
    }

    @Test
    void testRegister_Failure() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";

        when(passwordEncoder.encode(password)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<AuthenticationResponse> response = underTest.register(username, password);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(passwordEncoder).encode(password);
        verifyNoMoreInteractions(jwtService, authRepository);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        String refreshToken = "refreshtoken";
        String accessToken = "accesstoken";

        AuthenticationResponse expectedResponse = new AuthenticationResponse();
        expectedResponse.setAccessToken(accessToken);
        expectedResponse.setRefreshToken(refreshToken);

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setPassword(passwordEncoder.encode(password));
        authEntity.setIsActivated(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(jwtService.generateToken(authEntity)).thenReturn(accessToken);
        when(authRepository.save(authEntity)).thenReturn(null);

        // Act
        ResponseEntity<AuthenticationResponse> response = underTest.login(username, password);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testLogin_InactiveUser() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<AuthenticationResponse> response = underTest.login(username, password);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(jwtService, authRepository);
    }

    @Test
    void testGenerateAccessTokenFromRefreshToken_ValidToken() {
        // Arrange
        String username = "testuser";
        String refreshToken = "valid_refresh_token";
        AuthEntity authEntity = new AuthEntity();
        authEntity.setRefreshToken(refreshToken);
        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(jwtService.generateToken(authEntity)).thenReturn("access_token");

        // Act
        ResponseEntity<AccessTokenResponse> response = underTest.generateAccessTokenFromRefreshToken(username, refreshToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access_token", response.getBody().getAccessToken());
        verify(authRepository).findById(username);
        verify(jwtService).generateToken(authEntity);
        verifyNoMoreInteractions(authRepository, jwtService);
    }

    @Test
    void testGenerateAccessTokenFromRefreshToken_InvalidToken() {
        // Arrange
        String username = "testuser";
        String refreshToken = "invalid_refresh_token";
        AuthEntity authEntity = new AuthEntity();
        authEntity.setRefreshToken("valid_refresh_token");
        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));

        // Act
        ResponseEntity<AccessTokenResponse> response = underTest.generateAccessTokenFromRefreshToken(username, refreshToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository, jwtService);
    }

    @Test
    void testLogout_Success() {
        // Arrange
        String username = "testuser";
        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(authRepository.save(authEntity)).thenReturn(authEntity);

        // Act
        ResponseEntity<String> response = underTest.logout(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("You have been successfully logged out.", response.getBody());
        assertNull(authEntity.getRefreshToken());
        verify(authRepository).findById(username);
        verify(authRepository).save(authEntity);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testLogout_UserNotFound() {
        // Arrange
        String username = "testuser";
        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.logout(username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        String username = "testuser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String retypePassword = "newPassword";

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setPassword(oldPassword);
        authEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        authEntity.setLastLogin(new Timestamp(System.currentTimeMillis()));
        authEntity.setIsActivated(true);

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(passwordEncoder.encode(oldPassword)).thenReturn(oldPassword);
        when(authRepository.save(authEntity)).thenReturn(authEntity);

        // Act
        ResponseEntity<String> response = underTest.changePassword(username, oldPassword, newPassword, retypePassword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody());
    }

    @Test
    void testChangePassword_UserNotFound() {
        // Arrange
        String username = "testuser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String retypePassword = "newPassword";

        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.changePassword(username, oldPassword, newPassword, retypePassword);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testRequestPasswordResetToken_Success() {
        // Arrange
        String username = "testuser";

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(authRepository.save(authEntity)).thenReturn(authEntity);

        // Act
        ResponseEntity<String> response = underTest.requestPasswordResetToken(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authRepository).findById(username);
        verify(authRepository).save(authEntity);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testRequestPasswordResetToken_UserNotFound() {
        // Arrange
        String username = "testuser";

        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.requestPasswordResetToken(username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testResetPassword_Success() {
        // Arrange
        String username = "testuser";
        String token = "reset_token";
        String newPassword = "new_password";

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setResetToken(token);

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));
        when(authRepository.save(authEntity)).thenReturn(authEntity);

        // Act
        ResponseEntity<String> response = underTest.resetPassword(username, token, newPassword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successful", response.getBody());
    }

    @Test
    void testResetPassword_UserNotFound() {
        // Arrange
        String username = "testuser";
        String token = "reset_token";
        String newPassword = "new_password";

        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.resetPassword(username, token, newPassword);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testResetPassword_InvalidOrExpiredToken() {
        // Arrange
        String username = "testuser";
        String token = "reset_token";
        String newPassword = "new_password";

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setResetToken("another_token");

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));

        // Act
        ResponseEntity<String> response = underTest.resetPassword(username, token, newPassword);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid or expired reset token", response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testGetAllLibrarians_Success() {
        // Arrange
        List<AuthEntity> authEntities = new ArrayList<>();
        AuthEntity authEntity1 = new AuthEntity();
        authEntity1.setUsername("user1");
        authEntity1.setIsActivated(true);
        authEntities.add(authEntity1);
        AuthEntity authEntity2 = new AuthEntity();
        authEntity2.setUsername("user2");
        authEntity2.setIsActivated(false);
        authEntities.add(authEntity2);

        when(authRepository.findAll()).thenReturn(authEntities);

        // Act
        ResponseEntity<List<Librarian>> response = underTest.getAllLibrarians();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Librarian> librarians = response.getBody();
        assertEquals(2, librarians.size());
        assertEquals("user1", librarians.get(0).getUsername());
        assertTrue(librarians.get(0).isActive());
        assertEquals("user2", librarians.get(1).getUsername());
        assertFalse(librarians.get(1).isActive());
        verify(authRepository).findAll();
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testGetAllLibrarians_NoLibrariansFound() {
        // Arrange
        when(authRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Librarian>> response = underTest.getAllLibrarians();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findAll();
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testGetInactiveLibrarians_Success() {
        // Arrange
        List<AuthEntity> authEntities = new ArrayList<>();
        AuthEntity authEntity1 = new AuthEntity();
        authEntity1.setUsername("user1");
        authEntity1.setIsActivated(false);
        authEntities.add(authEntity1);
        AuthEntity authEntity2 = new AuthEntity();
        authEntity2.setUsername("user2");
        authEntity2.setIsActivated(false);
        authEntities.add(authEntity2);

        when(authRepository.findAllByIsActivatedFalse()).thenReturn(authEntities);

        // Act
        ResponseEntity<List<String>> response = underTest.getInactiveLibrarians();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<String> inactiveLibrarians = response.getBody();
        assertEquals(2, inactiveLibrarians.size());
        assertTrue(inactiveLibrarians.contains("user1"));
        assertTrue(inactiveLibrarians.contains("user2"));
        verify(authRepository).findAllByIsActivatedFalse();
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testGetInactiveLibrarians_NoInactiveLibrariansFound() {
        // Arrange
        when(authRepository.findAllByIsActivatedFalse()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = underTest.getInactiveLibrarians();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findAllByIsActivatedFalse();
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testActivateLibrarian_Deactivate_Success() {
        // Arrange
        String username = "user1";
        boolean activate = false;

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setIsActivated(!activate);

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));

        // Act
        ResponseEntity<String> response = underTest.activateLibrarian(username, activate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(username + " has been deactivated", response.getBody());
        assertFalse(authEntity.getIsActivated());
        verify(authRepository).findById(username);
        verify(authRepository).save(authEntity);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testActivateLibrarian_UserNotFound() {
        // Arrange
        String username = "user1";
        boolean activate = true;

        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.activateLibrarian(username, activate);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testDeleteLibrarian_Success() {
        // Arrange
        String username = "user1";
        boolean isActivated = false;

        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(username);
        authEntity.setIsActivated(isActivated);

        when(authRepository.findById(username)).thenReturn(Optional.of(authEntity));

        // Act
        ResponseEntity<String> response = underTest.deleteLibrarian(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(username + " deleted successfully.", response.getBody());
        verify(authRepository).findById(username);
        verify(authRepository).delete(authEntity);
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    void testDeleteLibrarian_UserNotFound() {
        // Arrange
        String username = "user1";

        when(authRepository.findById(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = underTest.deleteLibrarian(username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(authRepository).findById(username);
        verifyNoMoreInteractions(authRepository);
    }

}