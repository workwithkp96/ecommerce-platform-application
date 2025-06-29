package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
