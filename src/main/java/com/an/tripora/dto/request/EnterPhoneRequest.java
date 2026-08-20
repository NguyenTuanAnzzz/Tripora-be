package com.an.tripora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnterPhoneRequest {
    @NotBlank(message = "SDT không được để trống")
    private String phone;
}
