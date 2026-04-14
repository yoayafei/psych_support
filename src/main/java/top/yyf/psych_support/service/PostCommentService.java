package top.yyf.psych_support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.yyf.psych_support.entity.PostComment;
import top.yyf.psych_support.model.vo.CreateCommentVO;
import top.yyf.psych_support.model.vo.CreatePostVO;
import top.yyf.psych_support.model.vo.PostCommentVO;

public interface PostCommentService {
    /**
     * 创建评论
     */
    PostComment createComment(CreateCommentVO createCommentVO, Long userId);

    /**
     * 获取帖子评论列表
     */
    Page<PostCommentVO> getCommentsByPost(Long postId, Integer page, Integer size);

    /**
     * 获取用户评论列表
     */
    Page<PostCommentVO> getUserComments(Long userId, Integer page, Integer size);

    /**
     * 更新评论状态
     */
    boolean updateCommentStatus(Long commentId, Byte status, Long adminUserId);

    /**
     * 删除评论（逻辑删除）
     */
    boolean deleteComment(Long commentId, Long currentUserId);
}