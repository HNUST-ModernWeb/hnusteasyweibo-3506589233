package cn.edu.hnust.easyweibo.model;

public record UserStats(
        long postCount,
        long likeCount,
        long commentCount
) {
}
