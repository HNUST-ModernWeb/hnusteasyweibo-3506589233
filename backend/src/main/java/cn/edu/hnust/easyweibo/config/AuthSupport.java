package cn.edu.hnust.easyweibo.config;

import cn.edu.hnust.easyweibo.exception.ApiException;
import cn.edu.hnust.easyweibo.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public final class AuthSupport {
    private AuthSupport() {
    }

    public static User optionalUser(HttpServletRequest request) {
        Object user = request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        return user instanceof User currentUser ? currentUser : null;
    }

    public static User requireUser(HttpServletRequest request) {
        User user = optionalUser(request);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "请先登录");
        }
        return user;
    }

    public static String currentToken(HttpServletRequest request) {
        Object token = request.getAttribute(AuthInterceptor.CURRENT_TOKEN_ATTRIBUTE);
        return token instanceof String value ? value : null;
    }
}
