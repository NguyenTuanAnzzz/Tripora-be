package com.an.tripora.controllers;


import com.an.tripora.dto.request.LoginRequest;
import com.an.tripora.dto.request.RegisterRequest;
import com.an.tripora.dto.request.VerifyOtpRequest;
import com.an.tripora.dto.response.LoginResponse;
import com.an.tripora.dto.response.RegisterResponse;
import com.an.tripora.dto.response.VerifyOtpResponse;
import com.an.tripora.models.User;
import com.an.tripora.services.JwtService;
import com.an.tripora.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    @PostMapping("register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request){
        return service.register(request);
    }

    @PostMapping("verify-email")
    public VerifyOtpResponse verifyOTP(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        return service.verifyOTP(request);
    }

    @PostMapping("login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return service.login(request);

    }
}
