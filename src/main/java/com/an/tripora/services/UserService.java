package com.an.tripora.services;

import com.an.tripora.dto.request.*;
import com.an.tripora.dto.response.*;
import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import com.an.tripora.exceptions.BadRequestException;
import com.an.tripora.models.EmailVerification;
import com.an.tripora.models.User;
import com.an.tripora.repositories.EmailVerificationRepo;
import com.an.tripora.repositories.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
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
                otp,
                "Verify OTP to register"
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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        LoginResponse response = new LoginResponse();

        response.setAccess_token(
                jwtService.generateToken(request.getEmail())
        );

        response.setMessage("Successfully");

        return response;
    }


    public ResendOtpResponse resendOtp( ResendOtpRequest request) {
        User user = repo.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Email không tồn tại"));
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "Email đã được xác thực"
            );
        }if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        // 3. Generate OTP
        String otp = generateOtp();

        // 4. Hash OTP
        String otpHash = encoder.encode(otp);

        // 5. Tìm EmailVerification
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
        // 6. Save OTP mới
        emailVerificationRepo.save(verification);

        // 7. Gửi OTP
        emailService.sendOtp(
                user.getEmail(),
                otp,
                "Verify OTP to register"
        );

        // 8. Response
        ResendOtpResponse response =
                new ResendOtpResponse();

        response.setMessage("OTP đã được gửi lại");
        return response;
    }


    public GetNameResponse getName() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        String email = authentication.getName();

        User user = repo
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        GetNameResponse response = new GetNameResponse();
        response.setName(user.getName());

        return response;
    }

    public User loginWithGoogle(String email, String name) {

        Optional<User> existingUser =
                repo.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();

        user.setEmail(email);
        user.setName(name);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        return repo.save(user);
    }

    public ForgotPasswordResponse forgotPassword( ForgotPasswordRequest request) {
        User user = repo.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Email không tồn tại"));

        String otp = generateOtp();


        String otpHash = encoder.encode(otp);

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

        emailVerificationRepo.save(verification);

        emailService.sendOtp(
                user.getEmail(),
                otp,
                "Verify OTP Forgot Password"
        );

        ForgotPasswordResponse response =
                new ForgotPasswordResponse();

        response.setMessage("OTP đã được gửi lại");
        return  response;
    }


    public VerifyForgotPasswordResponse verifyForgotPassword( VerifyForgotPasswordRequest request) {
        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );

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



        // 8. Response
        VerifyForgotPasswordResponse response =
                new VerifyForgotPasswordResponse();

        response.setMessage(
                "Xác thực email thành công"
        );
        return response;
    }


    public ChangPasswordResponse changePassword(@Valid ChangPasswordRequest request) {
        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );
        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Không tìm thấy mã xác thực"
                                )
                        );
        user.setPassword(encoder.encode(request.getNewPassword()));

        repo.save(user);
        ChangPasswordResponse response = new ChangPasswordResponse();
        emailVerificationRepo.delete(verification);
        response.setMesssage("Chúc mừng bạn đã đổi mật khẩu thành công");
        return response;
    }
}
