package com.vectoredu.backend.dto.request.authenticationRequestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class VerifyUserDto {
    private String email;
    private String verificationCode;
}