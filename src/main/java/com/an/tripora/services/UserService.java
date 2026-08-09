package com.an.tripora.services;

import com.an.tripora.dto.request.LoginRequest;
import com.an.tripora.dto.request.RegisterRequest;
import com.an.tripora.dto.request.VerifyOtpRequest;
import com.an.tripora.dto.response.LoginResponse;
import com.an.tripora.dto.response.RegisterResponse;
import com.an.tripora.dto.response.VerifyOtpResponse;
import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import com.an.tripora.exceptions.BadRequestException;
import com.an.tripora.models.EmailVerification;
import com.an.tripora.models.User;
import com.an.tripora.repositories.EmailVerificationRepo;
import com.an.tripora.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailVerificationRepo emailVerificationRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public User saveUser(User user){
        return repo.save(user);
    }

    private String generateOtp() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = ThreadLocalRandom.current()
                    .nextInt(chars.length());

            otp.append(chars.charAt(index));
        }

        return otp.toString();
    }

    public RegisterResponse register(RegisterRequest request) {

        // 1. Kiểm tra email đã tồn tại
        User user = repo.findByEmail(request.getEmail())
                .orElse(null);

        if (user != null) {

            if (user.getStatus() == UserStatus.ACTIVE) {
                throw new BadRequestException(
                        "Email đã được đăng ký"
                );
            }

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new BadRequestException(
                        "Tài khoản đã bị khóa"
                );
            }

            // PENDING
            // Cho đăng ký lại và gửi OTP mới
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setPassword(
                    encoder.encode(request.getPassword())
            );
            user.setRole(Role.CUSTOMER);
            user.setStatus(UserStatus.PENDING);

            user = repo.save(user);

        } else {

            // 2. Tạo User mới
            user = new User();

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());

            user.setPassword(
                    encoder.encode(request.getPassword())
            );

            user.setRole(Role.CUSTOMER);
            user.setStatus(UserStatus.PENDING);

            user = repo.save(user);
        }

        // 3. Generate OTP
        String otp = generateOtp();

        // 4. Hash OTP
        String otpHash = encoder.encode(otp);

        // 5. Tìm verification hiện tại
        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseGet(EmailVerification::new);

        verification.setUser(user);
        verification.setOtp(otpHash);

        LocalDateTime now = LocalDateTime.now();

        verification.setCreatedAt(now);
        verification.setLastSentAt(now);
        verification.setExpiresAt(
                now.plusMinutes(5)
        );

        // 6. Save
        emailVerificationRepo.save(verification);

        // 7. Gửi OTP thật
        emailService.sendOtp(
                user.getEmail(),
                otp
        );

        // 8. Response
        RegisterResponse response = new RegisterResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        return response;
    }


    public VerifyOtpResponse verifyOTP(VerifyOtpRequest request) {

        // 1. Tìm User
        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );

        // 2. Kiểm tra trạng thái
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "Email đã được xác thực"
            );
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        // 3. Tìm EmailVerification
        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Không tìm thấy mã xác thực"
                                )
                        );

        // 4. Kiểm tra OTP hết hạn
        if (LocalDateTime.now().isAfter(
                verification.getExpiresAt()
        )) {
            throw new BadRequestException(
                    "Mã OTP đã hết hạn"
            );
        }

        // 5. Kiểm tra OTP
        if (!encoder.matches(
                request.getOtp(),
                verification.getOtp()
        )) {
            throw new BadRequestException(
                    "Mã OTP không chính xác"
            );
        }

        // 6. Xác thực email
        user.setStatus(UserStatus.ACTIVE);
        repo.save(user);

        // 7. Xóa OTP sau khi sử dụng
        emailVerificationRepo.delete(verification);

        // 8. Response
        VerifyOtpResponse response =
                new VerifyOtpResponse();

        response.setMessage(
                "Xác thực email thành công"
        );
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());

        return response;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        LoginResponse response = new LoginResponse();

        if(authentication.isAuthenticated()){
            response.setAccess_token(jwtService.generateToken(request.getEmail()));
            response.setMessage("Successfully");
            return response;
        }else {
            response.setAccess_token("");
            response.setMessage("Fail");
             return  response;

        }
    }


}
