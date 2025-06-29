package com.ecommerce.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private String type;
    private UserProfileDto user;
}
