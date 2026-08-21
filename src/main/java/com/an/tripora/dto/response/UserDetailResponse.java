package com.an.tripora.dto.response;

import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String avatar;

    private Role role;

    private UserStatus status;

    private LocalDateTime createdAt;
}