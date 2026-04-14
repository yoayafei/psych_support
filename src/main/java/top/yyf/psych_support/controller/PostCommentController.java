package top.yyf.psych_support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.entity.PostComment;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.vo.CreateCommentVO;
import top.yyf.psych_support.model.vo.PostCommentVO;
import top.yyf.psych_support.service.PostCommentService;
import top.yyf.psych_support.util.JwtUtils;

import java.time.LocalDateTime;

@Slf4j
@Tag(name = "帖子评论管理", description = "帖子评论的增删改查及相关操作")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @Autowired
    private JwtUtils jwtUtils; // 注入你的JWT工具类

    @Autowired
    private UserMapper userMapper; // 注入UserMapper用于获取用户信息

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<PostCommentVO> createComment(@Valid @RequestBody CreateCommentVO createCommentVO,
                                               @RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);

            var comment = postCommentService.createComment(createCommentVO, userId);

            // 转换为VO返回
            PostCommentVO vo = convertToVO(comment, true);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("创建评论失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户评论列表")
    @GetMapping("/user/{userId}")
    public Result<Page<PostCommentVO>> getUserComments(@PathVariable Long userId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.isEmpty()) {
                currentUserId = jwtUtils.getUserIdFromToken(token);
            }

            Page<PostCommentVO> comments = postCommentService.getUserComments(userId, page, size);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取用户评论列表失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除评论（仅评论者可删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = jwtUtils.getUserIdFromToken(token);

            boolean success = postCommentService.deleteComment(id, currentUserId);
            if (!success) {
                return Result.error("删除评论失败，可能评论不存在或非本人发布");
            }
            return Result.success(true);
        } catch (Exception e) {
            log.error("删除评论失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    // 辅助方法：转换为VO
    private PostCommentVO convertToVO(top.yyf.psych_support.entity.PostComment comment, boolean isOwner) {
        PostCommentVO vo = new PostCommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());

        if (comment.getCreatedAt() != null) {
            vo.setFormattedDate(comment.getCreatedAt().toString());
        } else {
            vo.setFormattedDate(LocalDateTime.now().toString());
        }

        vo.setIsOwner(isOwner);

        // 获取用户昵称
        if (isOwner || comment.getStatus() == 2) { // 只有自己或已审核的评论才显示昵称
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        }

        return vo;
    }
}