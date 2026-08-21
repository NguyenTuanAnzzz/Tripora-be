package com.an.tripora.dto.request;


import com.an.tripora.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {

    private Role role;
}