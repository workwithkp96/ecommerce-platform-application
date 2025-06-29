package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserStatus;
import com.ecommerce.user.kafka.UserEventProducer;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserEventProducer userEventProducer;

    @Transactional
    public UserProfileDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phoneNumber(registrationDto.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Send user registration event
        userEventProducer.sendUserRegistrationEvent(savedUser);

        return mapToUserProfileDto(savedUser);
    }

    public AuthResponseDto loginUser(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        String token = jwtService.generateToken(user);

        return AuthResponseDto.builder()
                .token(token)
                .type("Bearer")
                .user(mapToUserProfileDto(user))
                .build();
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);

        // Send user update event
        userEventProducer.sendUserUpdateEvent(updatedUser);

        return mapToUserProfileDto(updatedUser);
    }

    public void resetPassword(PasswordResetDto resetDto) {
        User user = userRepository.findByEmail(resetDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Send password reset event
        userEventProducer.sendPasswordResetEvent(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus().toString());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}