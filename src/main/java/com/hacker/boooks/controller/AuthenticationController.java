package com.hacker.boooks.controller;

import com.hacker.boooks.bean.AccessTokenResponse;
import com.hacker.boooks.bean.AuthenticationResponse;
import com.hacker.boooks.bean.Librarian;
import com.hacker.boooks.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Authentication Management", description = "APIs for librarian authentication")
@CrossOrigin(origins = "*")
@Slf4j
@SuppressWarnings("unused")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(summary = "Register New Librarian", description = "This API allows you to register a new librarian in the library system. The request should include the necessary details such as username, password, and other relevant information. After successful registration, the librarian's account will be inactive until it is activated by an existing librarian.")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestHeader("username") String username,
            @RequestHeader("password") String password) {
        return authenticationService.register(username, password);
    }

    @Operation(summary = "Login", description = "This API allows a librarian to log in to the library system. The request should include the username and password. Upon successful login, an authentication token will be generated and returned in the response. This token should be included in subsequent requests to authenticate the librarian.")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestHeader("username") String username,
            @RequestHeader("password") String password) {
        return authenticationService.login(username, password);
    }

    @Operation(summary = "Generate Access Token from Refresh Token", description = "This API allows a librarian to generate a new access token using a refresh token. The request should include the username and refresh token obtained during the login process. If the refresh token is valid and matches the one stored in the database, a new access token will be generated and returned in the response. This access token should be used for subsequent authenticated requests to the library system.")
    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponse> generateAccessTokenFromRefreshToken(
            @RequestHeader("username") String username,
            @RequestHeader("refresh-token") String refreshToken
    ) {
        return authenticationService.generateAccessTokenFromRefreshToken(username, refreshToken);
    }

    @Operation(summary = "Logout", description = "This API allows a librarian to log out and invalidate their current session.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("username") String username) {
        return authenticationService.logout(username);
    }

    @Operation(summary = "Change Password", description = "This API allows a logged-in librarian to change their password. The request should include the old password and the new password. The password will be updated if the old password matches the current password of the librarian.")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("username") String username,
            @RequestHeader("old-password") String oldPassword,
            @RequestHeader("new-password") String newPassword,
            @RequestHeader("retype-password") String retypePassword
    ) {
        return authenticationService.changePassword(username, oldPassword, newPassword, retypePassword);
    }

    @Operation(summary = "Request Password Reset Token", description = "This API allows a librarian to request a password reset token by providing their username.")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordResetToken(
            @RequestHeader("username") String username
    ) {
        return authenticationService.requestPasswordResetToken(username);
    }

    @Operation(summary = "Reset Password", description = "This API allows a librarian to reset their password by providing the password reset token and the new password.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestHeader("username") String username,
            @RequestHeader("token") String token,
            @RequestHeader("newPassword") String newPassword
    ) {
        return authenticationService.resetPassword(username, token, newPassword);
    }

    @Operation(summary = "Get All Librarians", description = "This API retrieves a list of all librarians in the library system. It returns information such as the username and status (active or inactive) of each librarian.")
    @GetMapping("/librarians")
    public ResponseEntity<List<Librarian>> getAllLibrarians() {
        return authenticationService.getAllLibrarians();
    }

    @Operation(summary = "Get Inactive Librarians", description = "This API retrieves a list of inactive librarians in the library system. It provides information about librarians whose accounts are registered but not yet activated.")
    @GetMapping("/librarians/inactive")
    public ResponseEntity<List<String>> getInactiveLibrarians() {
        return authenticationService.getInactiveLibrarians();
    }

    @Operation(summary = "Activate Librarian Account", description = "This API allows an existing librarian to activate the account of a specific librarian. It requires the ID of the librarian whose account needs to be activated. Once activated, the librarian can log in and access the library system.")
    @PutMapping("/librarians/activate")
    public ResponseEntity<String> activateLibrarian(
            @RequestHeader("username") String username,
            @RequestParam(name = "activate", defaultValue = "true") Boolean activate
    ) {
        return authenticationService.activateLibrarian(username, activate);
    }

    @Operation(summary = "Delete Librarian Account", description = "This API allows an existing librarian to delete the account of a specific librarian. It requires the username of the librarian whose account needs to be deleted. Once deleted, the librarian's access to the library system will be revoked.")
    @DeleteMapping("/librarians")
    public ResponseEntity<String> deleteLibrarian(@RequestHeader("username") String username) {
        return authenticationService.deleteLibrarian(username);
    }

}
