package com.brightloop.subscription.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.auth.dto.AuthResponse;
import com.brightloop.subscription.auth.dto.ForgotPasswordRequest;
import com.brightloop.subscription.auth.dto.LoginRequest;
import com.brightloop.subscription.auth.dto.RegisterRequest;
import com.brightloop.subscription.auth.dto.ResetPasswordRequest;
import com.brightloop.subscription.auth.dto.StaffCreateRequest;
import com.brightloop.subscription.auth.dto.UserSummary;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.common.MailService;
import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.security.JwtService;
import com.brightloop.subscription.user.PasswordResetToken;
import com.brightloop.subscription.user.PasswordResetTokenRepository;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AppProperties properties;

    public AuthService(UserRepository userRepository,
            PasswordResetTokenRepository resetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MailService mailService,
            AppProperties properties) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.properties = properties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        assertEmailAvailable(request.email());
        User user = User.builder()
                .email(normalize(request.email()))
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.CUSTOMER)
                .state(request.state())
                .gstin(blankToNull(request.gstin()))
                .preferredCurrency(request.preferredCurrency() == null ? "INR" : request.preferredCurrency())
                .autopayEnabled(false)
                .active(true)
                .build();
        return issue(userRepository.save(user));
    }

    @Transactional
    public AuthResponse createStaff(StaffCreateRequest request) {
        assertEmailAvailable(request.email());
        User user = User.builder()
                .email(normalize(request.email()))
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.STAFF)
                .state(request.state())
                .gstin(blankToNull(request.gstin()))
                .preferredCurrency("INR")
                .autopayEnabled(false)
                .active(true)
                .build();
        return issue(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalize(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issue(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(normalize(request.email())).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken reset = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .used(false)
                    .createdAt(Instant.now())
                    .build();
            resetTokenRepository.save(reset);

            String link = properties.frontendUrl() + "/reset-password?token=" + token;
            mailService.send(user.getEmail(), "Reset your Brightloop password",
                    "Use this link within 30 minutes to reset your password:\n\n" + link);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken reset = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> ApiException.badRequest("Reset token is invalid"));
        if (reset.isUsed()) {
            throw ApiException.badRequest("Reset token has already been used");
        }
        if (reset.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Reset token has expired");
        }
        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        reset.setUsed(true);
    }

    private AuthResponse issue(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.expirationMinutes(), UserSummary.from(user));
    }

    private void assertEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(normalize(email))) {
            throw ApiException.conflict("Email is already registered");
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
