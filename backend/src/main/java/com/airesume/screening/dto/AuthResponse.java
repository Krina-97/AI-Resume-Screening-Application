package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    String username;
    String role;
    String fullName;
}
