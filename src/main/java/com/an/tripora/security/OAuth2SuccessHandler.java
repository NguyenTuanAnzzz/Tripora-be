package com.an.tripora.security;

import com.an.tripora.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService service;

    @Autowired
    private com.an.tripora.services.JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        service.loginWithGoogle(email, name);
        String token = jwtService.generateToken(email);

        // Tạo cookie để truyền token về frontend một cách an toàn hơn
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("oauth2_auth_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Frontend cần đọc được để lưu vào localStorage
        cookie.setMaxAge(60); // Sống 60 giây là đủ để frontend lấy rồi xóa đi
        response.addCookie(cookie);

        // Redirect không kèm token trên URL nữa
        response.sendRedirect("http://localhost:3000/oauth2/redirect");
    }
}