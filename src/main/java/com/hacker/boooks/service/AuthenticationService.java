package com.hacker.boooks.service;

import com.hacker.boooks.bean.AccessTokenResponse;
import com.hacker.boooks.bean.AuthenticationResponse;
import com.hacker.boooks.bean.Librarian;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Authentication service.
 */
public interface AuthenticationService {

    /**
     * Registers a new user.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the response entity containing the authentication response
     */
    ResponseEntity<AuthenticationResponse> register(String username, String password);

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the response entity containing the authentication response
     */
    ResponseEntity<AuthenticationResponse> login(String username, String password);

    /**
     * Generates a new access token from a refresh token.
     *
     * @param username     the username of the user
     * @param refreshToken the refresh token
     * @return the response entity containing the new access token
     */
    ResponseEntity<AccessTokenResponse> generateAccessTokenFromRefreshToken(String username, String refreshToken);

    /**
     * Logs out the user by removing the refresh token.
     *
     * @param username the username of the user
     * @return the response entity with a success message
     */
    ResponseEntity<String> logout(String username);

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
    ResponseEntity<String> changePassword(String username, String oldPassword, String newPassword, String retypePassword);

    /**
     * Requests a password reset token for the user.
     *
     * @param username the username of the user
     * @return the response entity with the password reset token
     * @throws UsernameNotFoundException if the user is not found
     */
    ResponseEntity<String> requestPasswordResetToken(String username);

    /**
     * Resets the password for the user using the provided reset token.
     *
     * @param username    the username of the user
     * @param token       the reset token
     * @param newPassword the new password
     * @return the response entity indicating the result of password reset
     * @throws UsernameNotFoundException if the user is not found
     */
    ResponseEntity<String> resetPassword(String username, String token, String newPassword);

    /**
     * Retrieves a list of all librarians.
     *
     * @return the response entity containing the list of librarians
     */
    ResponseEntity<List<Librarian>> getAllLibrarians();

    /**
     * Retrieves a list of usernames of inactive librarians.
     *
     * @return the response entity containing the list of inactive librarians' usernames
     */
    ResponseEntity<List<String>> getInactiveLibrarians();

    /**
     * Activates or deactivates the account of a librarian.
     *
     * @param username the username of the librarian
     * @param activate a flag indicating whether to activate or deactivate the account
     * @return the response entity with the activation status message
     */
    ResponseEntity<String> activateLibrarian(String username, Boolean activate);

    /**
     * Deletes a librarian from the system.
     *
     * @param username the username of the librarian to delete
     * @return the response entity with the delete status message
     */
    ResponseEntity<String> deleteLibrarian(String username);

}
