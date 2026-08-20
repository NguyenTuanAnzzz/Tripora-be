package com.an.tripora.dto.request;

import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^[0-9]{10,11}$",
            message = "Số điện thoại phải có 10-11 chữ số"
    )
    private String phone;

    @NotNull(message = "Role không được để trống")
    private Role role;

    @NotNull(message = "Status không được để trống")
    private UserStatus status;

    private String avatar;
}