package cn.edu.hnust.easyweibo.controller;

import cn.edu.hnust.easyweibo.config.AuthSupport;
import cn.edu.hnust.easyweibo.dto.CommentRequest;
import cn.edu.hnust.easyweibo.dto.CommentResponse;
import cn.edu.hnust.easyweibo.dto.PostRequest;
import cn.edu.hnust.easyweibo.dto.PostResponse;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> list(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        return postService.list(topic, keyword, page, size, AuthSupport.optionalUser(request));
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id, HttpServletRequest request) {
        return postService.get(id, AuthSupport.optionalUser(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody PostRequest requestBody, HttpServletRequest request) {
        return postService.create(AuthSupport.requireUser(request), requestBody);
    }

    @PutMapping("/{id}")
    public PostResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest requestBody,
            HttpServletRequest request
    ) {
        return postService.update(id, AuthSupport.requireUser(request), requestBody);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        postService.delete(id, AuthSupport.requireUser(request));
    }

    @PostMapping("/{id}/like")
    public PostResponse toggleLike(@PathVariable Long id, HttpServletRequest request) {
        return postService.toggleLike(id, AuthSupport.requireUser(request));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse comment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest requestBody,
            HttpServletRequest request
    ) {
        return postService.comment(id, AuthSupport.requireUser(request), requestBody);
    }
}
