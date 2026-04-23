package cn.edu.hnust.easyweibo.service;

import cn.edu.hnust.easyweibo.dto.UpdateProfileRequest;
import cn.edu.hnust.easyweibo.dto.UserResponse;
import cn.edu.hnust.easyweibo.exception.ApiException;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String DEFAULT_AVATAR = "/default-avatar.svg";
    private static final String DEFAULT_MAJOR = "现代 Web 学习者";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
        return DtoMapper.toUserResponse(user, userRepository.stats(user.id()));
    }

    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        User user = userRepository.updateProfile(
                currentUser.id(),
                request.displayName().trim(),
                blankToDefault(request.major(), DEFAULT_MAJOR),
                blankToDefault(request.bio(), ""),
                blankToDefault(request.avatarUrl(), DEFAULT_AVATAR)
        );
        return DtoMapper.toUserResponse(user, userRepository.stats(user.id()));
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
