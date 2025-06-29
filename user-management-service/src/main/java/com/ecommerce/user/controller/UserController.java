package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserProfileDto user = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(@Valid @RequestBody UserLoginDto loginDto) {
        AuthResponseDto response = userService.loginUser(loginDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfileDto user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDto updateDto) {
        UserProfileDto user = userService.updateUserProfile(userId, updateDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        userService.resetPassword(resetDto);
        return ResponseEntity.ok("Password reset link sent to your email");
    }
}