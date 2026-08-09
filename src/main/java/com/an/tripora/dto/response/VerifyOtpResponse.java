package com.an.tripora.dto.response;

import com.an.tripora.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpResponse {

    private String message;

    private Long userId;

    private String email;

    private UserStatus status;
}