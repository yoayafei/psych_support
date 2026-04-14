package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yyf.psych_support.entity.Post;
import top.yyf.psych_support.entity.PostComment;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.PostCommentMapper;
import top.yyf.psych_support.mapper.PostMapper;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.vo.CreateCommentVO;
import top.yyf.psych_support.model.vo.PostCommentVO;
import top.yyf.psych_support.service.PostCommentService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    public PostComment createComment(CreateCommentVO createCommentVO, Long userId) {
        // 检查帖子是否存在且已审核
        Post post = postMapper.selectById(createCommentVO.getPostId());
        if (post == null || post.getDeleted() == 1 || post.getStatus() != 2) {
            throw new IllegalArgumentException("帖子不存在或未审核通过");
        }

        PostComment comment = new PostComment();
        comment.setPostId(createCommentVO.getPostId());
        comment.setUserId(userId);
        comment.setContent(createCommentVO.getContent());
        comment.setStatus((byte) 2); // 默认通过
        comment.setDeleted((byte) 0); // 默认未删除

        postCommentMapper.insert(comment);
        return comment;
    }

    @Override
    public Page<PostCommentVO> getCommentsByPost(Long postId, Integer page, Integer size) {
        // 验证帖子是否存在且已审核
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1 || post.getStatus() != 2) {
            throw new IllegalArgumentException("帖子不存在或未审核通过");
        }

        Page<PostComment> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, (byte) 2) // 只查询已审核的评论
                .eq(PostComment::getDeleted, (byte) 0) // 只查询未删除的评论
                .orderByAsc(PostComment::getCreatedAt);

        Page<PostComment> result = postCommentMapper.selectPage(mpPage, wrapper);

        return (Page<PostCommentVO>) result.convert(comment -> convertToVO(comment, false));
    }

    @Override
    public Page<PostCommentVO> getUserComments(Long userId, Integer page, Integer size) {
        Page<PostComment> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getUserId, userId)
                .eq(PostComment::getDeleted, (byte) 0) // 只查询未删除的评论
                .orderByDesc(PostComment::getCreatedAt);

        Page<PostComment> result = postCommentMapper.selectPage(mpPage, wrapper);

        return (Page<PostCommentVO>) result.convert(comment -> convertToVO(comment, true));
    }

    @Override
    public boolean updateCommentStatus(Long commentId, Byte status, Long adminUserId) {
        PostComment existing = postCommentMapper.selectById(commentId);
        if (existing == null || existing.getDeleted() == 1) {
            return false;
        }

        // 验证状态值
        if (status != 0 && status != 1 && status != 2) {
            throw new IllegalArgumentException("状态值必须是0、1或2");
        }

        PostComment update = new PostComment();
        update.setId(commentId);
        update.setStatus(status);

        int result = postCommentMapper.updateById(update);
        return result > 0;
    }

    @Override
    public boolean deleteComment(Long commentId, Long currentUserId) {
        // 查询评论是否存在且未被删除
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .eq(PostComment::getDeleted, (byte) 0); // 未删除的评论

        PostComment existing = postCommentMapper.selectOne(queryWrapper);
        if (existing == null || !existing.getUserId().equals(currentUserId)) {
            return false;
        }

        // 使用条件更新进行逻辑删除
        LambdaUpdateWrapper<PostComment> updateWrapper = new LambdaUpdateWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .eq(PostComment::getUserId, currentUserId) // 确保只能删除自己的评论
                .set(PostComment::getDeleted, (byte) 1);   // 设置deleted为1，表示已删除

        int result = postCommentMapper.update(null, updateWrapper);
        return result > 0;
    }

    private PostCommentVO convertToVO(PostComment comment, boolean includePrivateInfo) {
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
            vo.setFormattedDate(java.time.LocalDateTime.now().toString());
        }

        if (includePrivateInfo) {
            vo.setIsOwner(true);
        }

        // 获取用户昵称
        if (includePrivateInfo || comment.getStatus() == 2) { // 只有自己或已审核的评论才显示昵称
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
            }
        }

        return vo;
    }
}