package cn.edu.hnust.easyweibo.controller;

import cn.edu.hnust.easyweibo.config.AuthSupport;
import cn.edu.hnust.easyweibo.dto.AuthResponse;
import cn.edu.hnust.easyweibo.dto.LoginRequest;
import cn.edu.hnust.easyweibo.dto.RegisterRequest;
import cn.edu.hnust.easyweibo.dto.UserResponse;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.repository.UserRepository;
import cn.edu.hnust.easyweibo.service.AuthService;
import cn.edu.hnust.easyweibo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        User user = AuthSupport.requireUser(request);
        return userService.getPublicProfile(user.id());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        AuthSupport.requireUser(request);
        authService.logout(AuthSupport.currentToken(request));
    }
}
