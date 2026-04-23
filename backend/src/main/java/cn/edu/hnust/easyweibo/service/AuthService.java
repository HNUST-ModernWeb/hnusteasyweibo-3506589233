package cn.edu.hnust.easyweibo.service;

import cn.edu.hnust.easyweibo.dto.AuthResponse;
import cn.edu.hnust.easyweibo.dto.LoginRequest;
import cn.edu.hnust.easyweibo.dto.RegisterRequest;
import cn.edu.hnust.easyweibo.exception.ApiException;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.repository.AuthTokenRepository;
import cn.edu.hnust.easyweibo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {
    private static final String DEFAULT_MAJOR = "现代 Web 学习者";

    private final UserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int tokenDays;

    public AuthService(
            UserRepository userRepository,
            AuthTokenRepository tokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Value("${app.auth.token-days:7}") int tokenDays
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenDays = tokenDays;
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            User user = userRepository.create(
                    request.username().trim(),
                    passwordEncoder.encode(request.password()),
                    request.displayName().trim(),
                    blankToDefault(request.major(), DEFAULT_MAJOR),
                    blankToDefault(request.bio(), "")
            );
            return authResponse(user);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "用户名已存在");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误");
        }

        return authResponse(user);
    }

    public Optional<User> currentUser(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return tokenRepository.findActiveUserId(token).flatMap(userRepository::findById);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenRepository.revoke(token);
        }
    }

    private AuthResponse authResponse(User user) {
        String token = generateToken();
        tokenRepository.save(token, user.id(), LocalDateTime.now().plusDays(tokenDays));
        return new AuthResponse("Bearer", token, DtoMapper.toUserResponse(user, userRepository.stats(user.id())));
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
