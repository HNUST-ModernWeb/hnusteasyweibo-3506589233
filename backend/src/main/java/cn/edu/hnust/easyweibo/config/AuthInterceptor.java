package cn.edu.hnust.easyweibo.config;

import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";
    public static final String CURRENT_TOKEN_ATTRIBUTE = "currentToken";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = bearerToken(request);
        authService.currentUser(token).ifPresent(user -> {
            request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
            request.setAttribute(CURRENT_TOKEN_ATTRIBUTE, token);
        });
        return true;
    }

    private String bearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }
}
