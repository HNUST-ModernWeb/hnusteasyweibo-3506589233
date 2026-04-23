package cn.edu.hnust.easyweibo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authPostLikeCommentProfileAndUploadFlow() throws Exception {
        AuthSession owner = register("owner_" + System.nanoTime(), "Owner 同学");
        AuthSession stranger = register("stranger_" + System.nanoTime(), "Stranger 同学");

        HttpResponse<String> unauthenticated = send("GET", "/api/auth/me", null, null);
        assertThat(unauthenticated.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        HttpResponse<String> createdPost = send("POST", "/api/posts", owner.token(), Map.of(
                "content", "这是一条来自 Spring Boot 后端测试的校园动态。",
                "topic", "学习",
                "visibility", "全校可见",
                "imageUrl", ""
        ));
        assertThat(createdPost.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> createdPostBody = object(createdPost);
        Long postId = number(createdPostBody, "id");

        HttpResponse<String> listResponse = send("GET", "/api/posts?topic=学习", null, null);
        assertThat(listResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(list(listResponse)).isNotEmpty();

        HttpResponse<String> liked = send("POST", "/api/posts/" + postId + "/like", owner.token(), Map.of());
        assertThat(liked.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(number(object(liked), "likeCount")).isEqualTo(1);
        assertThat(object(liked)).containsEntry("likedByCurrentUser", true);

        HttpResponse<String> unliked = send("POST", "/api/posts/" + postId + "/like", owner.token(), Map.of());
        assertThat(unliked.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(number(object(unliked), "likeCount")).isEqualTo(0);

        HttpResponse<String> comment = send("POST", "/api/posts/" + postId + "/comments", owner.token(), Map.of(
                "content", "评论接口工作正常。"
        ));
        assertThat(comment.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(object(comment)).containsEntry("content", "评论接口工作正常。");

        HttpResponse<String> forbidden = send("PUT", "/api/posts/" + postId, stranger.token(), Map.of(
                "content", "试图编辑别人的动态。",
                "topic", "生活",
                "visibility", "全校可见",
                "imageUrl", ""
        ));
        assertThat(forbidden.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());

        HttpResponse<String> profile = send("PUT", "/api/users/me", owner.token(), Map.of(
                "displayName", "Owner Vue 后端版",
                "major", "JavaWeb 阶段",
                "bio", "正在测试用户资料接口。",
                "avatarUrl", "/uploads/avatar.png"
        ));
        assertThat(profile.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(object(profile)).containsEntry("displayName", "Owner Vue 后端版");

        HttpResponse<String> badUpload = upload(owner.token(), "note.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes(StandardCharsets.UTF_8));
        assertThat(badUpload.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        HttpResponse<String> upload = upload(owner.token(), "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        assertThat(upload.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat((String) object(upload).get("url")).startsWith("/uploads/");

        HttpResponse<String> deleted = send("DELETE", "/api/posts/" + postId, owner.token(), null);
        assertThat(deleted.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private AuthSession register(String username, String displayName) throws Exception {
        HttpResponse<String> response = send("POST", "/api/auth/register", null, Map.of(
                "username", username,
                "password", "secret123",
                "displayName", displayName,
                "major", "现代 Web 课程",
                "bio", "测试用户"
        ));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        Map<String, Object> body = object(response);
        Map<?, ?> user = (Map<?, ?>) body.get("user");
        return new AuthSession((String) body.get("token"), ((Number) user.get("id")).longValue());
    }

    private HttpResponse<String> send(String method, String path, String token, Object body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> upload(String token, String filename, String contentType, byte[] bytes) throws Exception {
        String boundary = "----easy-weibo-test-boundary";
        byte[] body = multipartBody(boundary, filename, contentType, bytes);
        HttpRequest request = HttpRequest.newBuilder(uri("/api/uploads"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private byte[] multipartBody(String boundary, String filename, String contentType, byte[] bytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(bytes);
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private Map<String, Object> object(HttpResponse<String> response) throws Exception {
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    private List<Object> list(HttpResponse<String> response) throws Exception {
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    private Long number(Map<?, ?> map, String key) {
        return ((Number) map.get(key)).longValue();
    }

    private record AuthSession(String token, Long userId) {
    }
}
