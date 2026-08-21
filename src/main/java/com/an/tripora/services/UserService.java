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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Autowired
    private CloudinaryService cloudinaryService;

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
            if (repo.existsByPhoneAndIdNot(
                    request.getPhone(),
                    user.getId()
            )) {
                throw new BadRequestException(
                        "Số điện thoại đã được sử dụng"
                );
            }

            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setPassword(
                    encoder.encode(request.getPassword())
            );
            user.setRole(Role.CUSTOMER);
            user.setStatus(UserStatus.PENDING);

            user = repo.save(user);

        } else {

            // User mới
            if (repo.existsByPhone(request.getPhone())) {
                throw new BadRequestException(
                        "Số điện thoại đã được sử dụng"
                );
            }

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

        String otp = generateOtp();
        String otpHash = encoder.encode(otp);

        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseGet(EmailVerification::new);

        verification.setUser(user);
        verification.setOtp(otpHash);
        verification.setVerified(false);

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
                "Verify OTP to register"
        );

        RegisterResponse response = new RegisterResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

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
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

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


    public ResendOtpResponse resendOtp(
            ResendOtpRequest request
    ) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );

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

        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseGet(EmailVerification::new);

        LocalDateTime now = LocalDateTime.now();

        if (verification.getLastSentAt() != null
                && now.isBefore(
                verification.getLastSentAt()
                        .plusMinutes(1)
        )) {

            throw new BadRequestException(
                    "Vui lòng đợi 1 phút trước khi gửi lại OTP"
            );
        }

        String otp = generateOtp();
        String otpHash = encoder.encode(otp);

        verification.setUser(user);
        verification.setOtp(otpHash);
        verification.setVerified(false);
        verification.setCreatedAt(now);
        verification.setLastSentAt(now);
        verification.setExpiresAt(
                now.plusMinutes(5)
        );

        emailVerificationRepo.save(verification);

        emailService.sendOtp(
                user.getEmail(),
                otp,
                "Verify OTP to register"
        );

        ResendOtpResponse response =
                new ResendOtpResponse();

        response.setMessage("OTP đã được gửi lại");

        return response;
    }


    public User loginWithGoogle(
            String email,
            String name,
            String avatar
    ) {

        Optional<User> existingUser =
                repo.findByEmail(email);

        if (existingUser.isPresent()) {

            User user = existingUser.get();

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new BadRequestException(
                        "Tài khoản đã bị khóa"
                );
            }

            // Có thể cập nhật thông tin Google mới nhất
            user.setName(name);
            user.setAvatar(avatar);

            return repo.save(user);
        }

        User user = new User();

        user.setEmail(email);
        user.setName(name);
        user.setAvatar(avatar);
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        return repo.save(user);
    }

    public ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    ) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        String otp = generateOtp();
        String otpHash = encoder.encode(otp);

        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseGet(EmailVerification::new);

        LocalDateTime now = LocalDateTime.now();

        verification.setUser(user);
        verification.setOtp(otpHash);
        verification.setVerified(false);
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

        response.setMessage("OTP đã được gửi");

        return response;
    }


    public VerifyForgotPasswordResponse verifyForgotPassword(
            VerifyForgotPasswordRequest request
    ) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email không tồn tại"
                        )
                );
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        EmailVerification verification =
                emailVerificationRepo.findByUser(user)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Không tìm thấy mã xác thực"
                                )
                        );

        if (LocalDateTime.now().isAfter(
                verification.getExpiresAt()
        )) {
            throw new BadRequestException(
                    "Mã OTP đã hết hạn"
            );
        }

        if (!encoder.matches(
                request.getOtp(),
                verification.getOtp()
        )) {
            throw new BadRequestException(
                    "Mã OTP không chính xác"
            );
        }

        verification.setVerified(true);
        emailVerificationRepo.save(verification);

        VerifyForgotPasswordResponse response =
                new VerifyForgotPasswordResponse();

        response.setMessage(
                "Xác thực OTP thành công"
        );

        return response;
    }


    public ChangPasswordResponse changePassword(
            @Valid ChangPasswordRequest request
    ) {

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

        if (!verification.isVerified()) {
            throw new BadRequestException(
                    "Bạn chưa xác thực OTP"
            );
        }

        if (LocalDateTime.now().isAfter(
                verification.getExpiresAt()
        )) {
            throw new BadRequestException(
                    "Mã OTP đã hết hạn"
            );
        }

        user.setPassword(
                encoder.encode(request.getNewPassword())
        );

        repo.save(user);

        emailVerificationRepo.delete(verification);

        ChangPasswordResponse response =
                new ChangPasswordResponse();

        response.setMesssage(
                "Chúc mừng bạn đã đổi mật khẩu thành công"
        );

        return response;
    }

    public GetInfoResponse getInfo() {
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

        GetInfoResponse response = new GetInfoResponse ();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;

    }

    public EnterPhoneResponse enterPhone(
            EnterPhoneRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = repo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        if (repo.existsByPhoneAndIdNot(
                request.getPhone(),
                user.getId()
        )) {
            throw new BadRequestException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        user.setPhone(request.getPhone());

        repo.save(user);

        EnterPhoneResponse response =
                new EnterPhoneResponse();

        response.setMessage("Thanh cong");

        return response;
    }


    public Page<GetAllUsersResponse> getAllUsers(
            int page,
            int size,
            String keyword,
            Role role,
            UserStatus status
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = repo.searchUsers(
                keyword,
                role,
                status,
                pageable
        );

        return users.map(user -> {
            GetAllUsersResponse response = new GetAllUsersResponse();

            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setRole(user.getRole());
            response.setStatus(user.getStatus());
            response.setAvatar(user.getAvatar());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());

            return response;
        });
    }


    public UpdateProfileResponse updateProfile(
            @Valid UpdateProfileRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = repo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        if (repo.existsByPhoneAndIdNot(
                request.getPhone(),
                user.getId()
        )) {
            throw new BadRequestException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        user.setName(request.getName());
        user.setPhone(request.getPhone());

        // Nếu user có upload avatar mới
        if (request.getAvatar() != null
                && !request.getAvatar().isEmpty()) {

            String avatarUrl =
                    cloudinaryService.uploadImage(
                            request.getAvatar()
                    );

            user.setAvatar(avatarUrl);
        }

        repo.save(user);

        UpdateProfileResponse response =
                new UpdateProfileResponse();

        response.setMessage("Successfully");

        return response;
    }

    public CreateUserResponse creatUser(CreateUserRequest request) {

        // 1. Kiểm tra email đã tồn tại
        if (repo.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException(
                    "Email đã được sử dụng"
            );
        }

        // 2. Kiểm tra phone đã tồn tại
        if (repo.existsByPhone(request.getPhone())) {
            throw new BadRequestException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        // 3. Tạo User
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Không lưu password dạng plain text
        user.setPassword(
                encoder.encode(request.getPassword())
        );

        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setAvatar(request.getAvatar());

        // 4. Lưu database
        user = repo.save(user);

        // 5. Tạo response
        CreateUserResponse response =
                new CreateUserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setAvatar(user.getAvatar());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setMessage(
                "Tạo tài khoản thành công"
        );

        return response;
    }

    public UpdateRoleResponse updateRole(
            Long id,
            UpdateRoleRequest request
    ) {

        User user = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // User hiện tại đã là ADMIN thì không cho sửa role
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException(
                    "Không thể thay đổi role của ADMIN"
            );
        }

        user.setRole(request.getRole());
        repo.save(user);

        UpdateRoleResponse response = new UpdateRoleResponse();
        response.setMessage("Successfully");

        return response;
    }

    public UserDetailResponse userDetail(Long id) {

        User user = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        UserDetailResponse response = new UserDetailResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setAvatar(user.getAvatar());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
