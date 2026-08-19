package com.an.tripora.controllers;


import com.an.tripora.dto.request.*;
import com.an.tripora.dto.response.*;
import com.an.tripora.services.JwtService;
import com.an.tripora.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    @PostMapping("auth/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request){
        return service.register(request);
    }

    @PostMapping("auth/verify-email")
    public VerifyOtpResponse verifyOTP(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        return service.verifyOTP(request);
    }

    @PostMapping("auth/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return service.login(request);

    }

    @PostMapping("auth/resend-otp")
    public ResendOtpResponse ResendOtp(@Valid @RequestBody ResendOtpRequest request){
        return  service.resendOtp(request);
    }


    @PostMapping("auth/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        return service.forgotPassword(request);
    }

    @PostMapping("auth/verify-forgot-password")
    public VerifyForgotPasswordResponse verifyForgotPassword(@Valid @RequestBody VerifyForgotPasswordRequest request){
        return service.verifyForgotPassword(request);
    }

    @PostMapping("auth/change-password")
    public ChangPasswordResponse changePassword(@Valid @RequestBody ChangPasswordRequest request){
        return service.changePassword(request);
    }

    @GetMapping("get-info")
    public GetInfoResponse getInfo(){
        return  service.getInfo();
    }
}
