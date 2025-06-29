package com.ecommerce.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;
}
