package cn.edu.hnust.easyweibo.service;

import cn.edu.hnust.easyweibo.dto.CommentResponse;
import cn.edu.hnust.easyweibo.dto.PostResponse;
import cn.edu.hnust.easyweibo.dto.UserResponse;
import cn.edu.hnust.easyweibo.model.Comment;
import cn.edu.hnust.easyweibo.model.PostSummary;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.model.UserStats;

import java.util.List;

final class DtoMapper {
    private DtoMapper() {
    }

    static UserResponse toUserResponse(User user, UserStats stats) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.displayName(),
                user.major(),
                user.bio(),
                user.avatarUrl(),
                user.role(),
                stats.postCount(),
                stats.likeCount(),
                stats.commentCount(),
                user.createdAt()
        );
    }

    static PostResponse toPostResponse(PostSummary post, List<Comment> comments) {
        return new PostResponse(
                post.id(),
                post.userId(),
                post.authorName(),
                post.authorMajor(),
                post.authorAvatar(),
                post.content(),
                post.topic(),
                post.visibility(),
                post.imageUrl(),
                post.likeCount(),
                post.commentCount(),
                post.likedByCurrentUser(),
                comments.stream().map(DtoMapper::toCommentResponse).toList(),
                post.createdAt(),
                post.updatedAt()
        );
    }

    static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.id(),
                comment.userId(),
                comment.authorName(),
                comment.authorAvatar(),
                comment.content(),
                comment.createdAt()
        );
    }
}
