package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.AccessTokenResponse;
import com.hacker.boooks.bean.AuthenticationResponse;
import com.hacker.boooks.bean.Librarian;
import com.hacker.boooks.config.JwtService;
import com.hacker.boooks.entity.AuthEntity;
import com.hacker.boooks.exception.InactiveUserException;
import com.hacker.boooks.repository.AuthRepository;
import com.hacker.boooks.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Authentication service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String USER_NOT_FOUND = "User not found";
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the response entity containing the authentication response
     */
    @Override
    public ResponseEntity<AuthenticationResponse> register(String username, String password) {
        try {
            var user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .roles("ADMIN")
                    .build();

            String refreshToken = generateRefreshToken(username);

            AuthEntity authEntity = new AuthEntity();
            authEntity.setUsername(username);
            authEntity.setPassword(passwordEncoder.encode(password));
            authEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
            authEntity.setLastLogin(new Timestamp(System.currentTimeMillis()));
            authEntity.setRefreshToken(refreshToken);
            authEntity.setIsActivated(false);

            authRepository.save(authEntity);

            String accessToken = jwtService.generateToken(authEntity);

            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setAccessToken(accessToken);
            authenticationResponse.setRefreshToken(refreshToken);

            log.info("User registered successfully: {}", username);

            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            log.error("Error occurred during user registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the response entity containing the authentication response
     */
    @Override
    public ResponseEntity<AuthenticationResponse> login(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            if (Boolean.FALSE.equals(authEntity.getIsActivated())) {
                log.error("Inactive user: {}", username);
                throw new InactiveUserException("User account is inactive");
            }

            String refreshToken = generateRefreshToken(username);

            authEntity.setLastLogin(new Timestamp(System.currentTimeMillis()));
            authEntity.setRefreshToken(refreshToken);
            authRepository.save(authEntity);

            String accessToken = jwtService.generateToken(authEntity);

            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setAccessToken(accessToken);
            authenticationResponse.setRefreshToken(refreshToken);

            log.info("User logged in successfully: {}", username);

            return ResponseEntity.ok(authenticationResponse);
        } catch (AuthenticationException e) {
            log.error("Failed to authenticate user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (InactiveUserException e) {
            log.error("Inactive user: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error occurred during user authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generates a new access token from a refresh token.
     *
     * @param username     the username of the user
     * @param refreshToken the refresh token
     * @return the response entity containing the new access token
     */
    @Override
    public ResponseEntity<AccessTokenResponse> generateAccessTokenFromRefreshToken(String username, String refreshToken) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            if (authEntity.getRefreshToken().equals(refreshToken)) {
                AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
                accessTokenResponse.setAccessToken(jwtService.generateToken(authEntity));
                return ResponseEntity.ok(accessTokenResponse);
            } else {
                log.error("Invalid refresh token for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error occurred during token generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Logs out the user by removing the refresh token.
     *
     * @param username the username of the user
     * @return the response entity with a success message
     */
    @Override
    public ResponseEntity<String> logout(String username) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            authEntity.setRefreshToken(null);
            authRepository.save(authEntity);

            return ResponseEntity.ok("You have been successfully logged out.");
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error occurred during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Changes the password for the user.
     *
     * @param username       the username of the user
     * @param oldPassword    the old password
     * @param newPassword    the new password
     * @param retypePassword the retype password
     * @return the response entity with a success message
     * @throws IllegalArgumentException if the old password is invalid or the new passwords do not match
     */
    @Override
    public ResponseEntity<String> changePassword(String username, String oldPassword, String newPassword, String retypePassword) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            if (authEntity.getPassword().equals(passwordEncoder.encode(oldPassword)) && newPassword.equals(retypePassword)) {
                authEntity.setPassword(passwordEncoder.encode(newPassword));
                authRepository.save(authEntity);

                return ResponseEntity.ok("Password updated successfully");
            } else {
                throw new IllegalArgumentException("Invalid old password or mismatched new passwords");
            }
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid password change request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred during password change: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Requests a password reset token for the user.
     *
     * @param username the username of the user
     * @return the response entity with the password reset token
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public ResponseEntity<String> requestPasswordResetToken(String username) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            String resetToken = generateResetToken();

            authEntity.setResetToken(resetToken);
            authRepository.save(authEntity);

            return ResponseEntity.ok(resetToken);
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error occurred while requesting password reset token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resets the password for the user using the provided reset token.
     *
     * @param username    the username of the user
     * @param token       the reset token
     * @param newPassword the new password
     * @return the response entity indicating the result of password reset
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public ResponseEntity<String> resetPassword(String username, String token, String newPassword) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            if (token.equals(authEntity.getResetToken())) {
                authEntity.setPassword(passwordEncoder.encode(newPassword));
                authEntity.setRefreshToken(null);
                authEntity.setResetToken(null);

                authRepository.save(authEntity);

                return ResponseEntity.ok("Password reset successful");
            } else {
                log.error("Invalid or expired reset token for user: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired reset token");
            }
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error occurred while resetting password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a list of all librarians.
     *
     * @return the response entity containing the list of librarians
     */
    @Override
    public ResponseEntity<List<Librarian>> getAllLibrarians() {
        try {
            List<AuthEntity> authEntities = authRepository.findAll();
            List<Librarian> librarians = new ArrayList<>();

            for (AuthEntity authEntity : authEntities) {
                Librarian librarian = new Librarian();
                librarian.setUsername(authEntity.getUsername());
                librarian.setActive(authEntity.getIsActivated());
                librarians.add(librarian);
            }

            if (librarians.isEmpty()) {
                log.warn("No librarians found");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(librarians);
        } catch (Exception e) {
            log.error("Error occurred while retrieving librarians: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a list of usernames of inactive librarians.
     *
     * @return the response entity containing the list of inactive librarians' usernames
     */
    @Override
    public ResponseEntity<List<String>> getInactiveLibrarians() {
        try {
            List<AuthEntity> authEntities = authRepository.findAllByIsActivatedFalse();
            List<String> inactiveLibrarians = authEntities.stream()
                    .map(AuthEntity::getUsername)
                    .toList();

            if (inactiveLibrarians.isEmpty()) {
                log.warn("No inactive librarians found");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(inactiveLibrarians);
        } catch (Exception e) {
            log.error("Error occurred while retrieving inactive librarians: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activates or deactivates the account of a librarian.
     *
     * @param username the username of the librarian
     * @param activate a flag indicating whether to activate or deactivate the account
     * @return the response entity with the activation status message
     */
    @Override
    public ResponseEntity<String> activateLibrarian(String username, Boolean activate) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            authEntity.setIsActivated(activate);
            authRepository.save(authEntity);

            String statusMessage = Boolean.TRUE.equals(activate) ? "activated" : "deactivated";
            return ResponseEntity.ok(username + " has been " + statusMessage);
        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", username);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error occurred while activating/deactivating librarian: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a librarian from the system.
     *
     * @param username the username of the librarian to delete
     * @return the response entity with the delete status message
     */
    @Override
    public ResponseEntity<String> deleteLibrarian(String username) {
        try {
            AuthEntity authEntity = authRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));

            if (Boolean.FALSE.equals(authEntity.getIsActivated())) {
                authRepository.delete(authEntity);
                return ResponseEntity.ok(username + " deleted successfully.");
            } else {
                throw new IllegalArgumentException("Cannot delete an active user. Deactivate the user first.");
            }
        } catch (UsernameNotFoundException e) {
            log.error(USER_NOT_FOUND + ": {}", username);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Error occurred while deleting librarian: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while deleting librarian: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    private String generateRefreshToken(String username) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);

        byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);

        byte[] combinedBytes = new byte[randomBytes.length + usernameBytes.length];
        System.arraycopy(randomBytes, 0, combinedBytes, 0, randomBytes.length);
        System.arraycopy(usernameBytes, 0, combinedBytes, randomBytes.length, usernameBytes.length);

        String base64Token = Base64.getUrlEncoder().withoutPadding().encodeToString(combinedBytes);
        return base64Token.substring(0, 256);
    }

    private String generateResetToken() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        byte[] keyBytes = new byte[64];
        secureRandom.nextBytes(keyBytes);
        for (byte b : keyBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.substring(0, 64);
    }

}
