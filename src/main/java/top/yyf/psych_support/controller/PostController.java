package top.yyf.psych_support.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.entity.Post;
import top.yyf.psych_support.entity.PostComment;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.PostCommentMapper;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.vo.CreatePostVO;
import top.yyf.psych_support.model.vo.PostCommentVO;
import top.yyf.psych_support.model.vo.PostVO;
import top.yyf.psych_support.service.PostCommentService;
import top.yyf.psych_support.service.PostService;
import top.yyf.psych_support.util.JwtUtils;

import java.time.LocalDateTime;

@Slf4j
@Tag(name = "社区帖子管理", description = "社区帖子的增删改查及相关操作")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostCommentService postCommentService;
    private final PostCommentMapper postCommentMapper;

    @Autowired
    private JwtUtils jwtUtils; // 注入你的JWT工具类

    @Autowired
    private UserMapper userMapper; // 注入UserMapper用于获取用户信息

    @Operation(summary = "创建帖子")
    @PostMapping
    public Result<PostVO> createPost(@Valid @RequestBody CreatePostVO createPostVO,
                                     @RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);

            var post = postService.createPost(createPostVO, userId);

            // 转换为VO返回
            PostVO vo = convertToVO(post, true);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("创建帖子失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取帖子列表（分页）")
    @GetMapping
    public Result<Page<PostVO>> getPosts(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Byte status,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.isEmpty()) {
                currentUserId = jwtUtils.getUserIdFromToken(token);
            }

            Page<PostVO> posts = postService.getPosts(page, size, status);
            return Result.success(posts);
        } catch (Exception e) {
            log.error("获取帖子列表失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户发布的帖子列表")
    @GetMapping("/user/{userId}")
    public Result<Page<PostVO>> getUserPosts(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.isEmpty()) {
                currentUserId = jwtUtils.getUserIdFromToken(token);
            }

            Page<PostVO> posts = postService.getUserPosts(userId, page, size);
            return Result.success(posts);
        } catch (Exception e) {
            log.error("获取用户帖子列表失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.isEmpty()) {
                currentUserId = jwtUtils.getUserIdFromToken(token);
            }

            PostVO post = postService.getPostDetail(id, currentUserId);
            if (post == null) {
                return Result.error("帖子不存在或无权访问");
            }
            return Result.success(post);
        } catch (Exception e) {
            log.error("获取帖子详情失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除帖子（仅作者可删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id,
                                      @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = jwtUtils.getUserIdFromToken(token);

            boolean success = postService.deletePost(id, currentUserId);
            if (!success) {
                return Result.error("删除帖子失败，可能帖子不存在或非本人发布");
            }
            return Result.success(true);
        } catch (Exception e) {
            log.error("删除帖子失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取帖子的评论列表")
    @GetMapping("/{postId}/comments")
    public Result<Page<PostCommentVO>> getComments(@PathVariable Long postId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.isEmpty()) {
                currentUserId = jwtUtils.getUserIdFromToken(token);
            }

            Page<PostCommentVO> comments = postCommentService.getCommentsByPost(postId, page, size);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论列表失败: ", e);
            return Result.error(e.getMessage());
        }
    }

    // 辅助方法：转换为VO
    private PostVO convertToVO(Post post, boolean includePrivateInfo) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setStatus(post.getStatus());
        vo.setViewCount(post.getViewCount());
        vo.setCreatedAt(post.getCreatedAt());

        if (post.getCreatedAt() != null) {
            vo.setFormattedDate(post.getCreatedAt().toString());
        } else {
            vo.setFormattedDate(LocalDateTime.now().toString());
        }

        vo.setIsOwner(includePrivateInfo);

        // 获取用户昵称
        if (includePrivateInfo || post.getStatus() == 2) { // 只有自己或已审核的帖子才显示昵称
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        }

        // 获取评论数量
        LambdaQueryWrapper<PostComment> commentWrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, post.getId())
                .eq(PostComment::getStatus, (byte) 2) // 只统计已审核的评论
                .eq(PostComment::getDeleted, (byte) 0); // 未删除的评论
        vo.setCommentCount(postCommentMapper.selectCount(commentWrapper).intValue());

        return vo;
    }
}