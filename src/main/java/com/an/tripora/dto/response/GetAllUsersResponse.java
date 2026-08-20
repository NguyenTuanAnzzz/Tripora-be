package com.an.tripora.dto.response;


import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllUsersResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private String avatar;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
