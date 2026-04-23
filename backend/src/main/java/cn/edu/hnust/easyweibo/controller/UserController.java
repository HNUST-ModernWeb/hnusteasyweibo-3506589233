package cn.edu.hnust.easyweibo.controller;

import cn.edu.hnust.easyweibo.config.AuthSupport;
import cn.edu.hnust.easyweibo.dto.UpdateProfileRequest;
import cn.edu.hnust.easyweibo.dto.UserResponse;
import cn.edu.hnust.easyweibo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.getPublicProfile(id);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest requestBody, HttpServletRequest request) {
        return userService.updateProfile(AuthSupport.requireUser(request), requestBody);
    }
}
