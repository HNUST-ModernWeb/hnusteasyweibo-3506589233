package cn.edu.hnust.easyweibo.service;

import cn.edu.hnust.easyweibo.dto.CommentRequest;
import cn.edu.hnust.easyweibo.dto.CommentResponse;
import cn.edu.hnust.easyweibo.dto.PostRequest;
import cn.edu.hnust.easyweibo.dto.PostResponse;
import cn.edu.hnust.easyweibo.exception.ApiException;
import cn.edu.hnust.easyweibo.model.Comment;
import cn.edu.hnust.easyweibo.model.Post;
import cn.edu.hnust.easyweibo.model.PostSummary;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.repository.CommentRepository;
import cn.edu.hnust.easyweibo.repository.LikeRepository;
import cn.edu.hnust.easyweibo.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    public List<PostResponse> list(String topic, String keyword, int page, int size, User currentUser) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        Long viewerId = currentUser == null ? null : currentUser.id();
        return postRepository.findAll(topic, keyword, safePage, safeSize, viewerId).stream()
                .map(summary -> DtoMapper.toPostResponse(summary, commentRepository.findByPostId(summary.id())))
                .toList();
    }

    public PostResponse get(Long id, User currentUser) {
        return response(id, currentUser);
    }

    public PostResponse create(User currentUser, PostRequest request) {
        Post post = postRepository.create(
                currentUser.id(),
                request.content().trim(),
                request.topic().trim(),
                request.visibility().trim(),
                blankToNull(request.imageUrl())
        );
        return response(post.id(), currentUser);
    }

    public PostResponse update(Long id, User currentUser, PostRequest request) {
        Post post = requirePost(id);
        requireOwner(post, currentUser);
        postRepository.update(
                id,
                request.content().trim(),
                request.topic().trim(),
                request.visibility().trim(),
                blankToNull(request.imageUrl())
        );
        return response(id, currentUser);
    }

    public void delete(Long id, User currentUser) {
        Post post = requirePost(id);
        requireOwner(post, currentUser);
        postRepository.delete(id);
    }

    public PostResponse toggleLike(Long id, User currentUser) {
        requirePost(id);
        likeRepository.toggle(id, currentUser.id());
        return response(id, currentUser);
    }

    public CommentResponse comment(Long id, User currentUser, CommentRequest request) {
        requirePost(id);
        Comment comment = commentRepository.create(id, currentUser.id(), request.content().trim());
        return DtoMapper.toCommentResponse(comment);
    }

    private PostResponse response(Long id, User currentUser) {
        Long viewerId = currentUser == null ? null : currentUser.id();
        PostSummary summary = postRepository.findSummaryById(id, viewerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "动态不存在"));
        return DtoMapper.toPostResponse(summary, commentRepository.findByPostId(id));
    }

    private Post requirePost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "动态不存在"));
    }

    private void requireOwner(Post post, User currentUser) {
        if (!post.userId().equals(currentUser.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "只能操作自己的动态");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
