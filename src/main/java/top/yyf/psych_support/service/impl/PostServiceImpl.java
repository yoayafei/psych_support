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
import top.yyf.psych_support.model.vo.CreatePostVO;
import top.yyf.psych_support.model.vo.PostVO;
import top.yyf.psych_support.service.PostService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostCommentMapper postCommentMapper;

    @Override
    public Post createPost(CreatePostVO createPostVO, Long userId) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(createPostVO.getTitle());
        post.setContent(createPostVO.getContent());
        post.setStatus((byte) 2); // 默认通过
        post.setViewCount(0);
        post.setDeleted((byte) 0); // 默认未删除

        postMapper.insert(post);
        return post;
    }

    @Override
    public Page<PostVO> getPosts(Integer page, Integer size, Byte status) {
        Page<Post> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(status != null, Post::getStatus, status)
                .eq(Post::getDeleted, (byte) 0) // 只查询未删除的
                .orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(mpPage, wrapper);

        return (Page<PostVO>) result.convert(post -> convertToVO(post, false));
    }

    @Override
    public Page<PostVO> getUserPosts(Long userId, Integer page, Integer size) {
        Page<Post> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getDeleted, (byte) 0) // 只查询未删除的
                .orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(mpPage, wrapper);

        return (Page<PostVO>) result.convert(post -> convertToVO(post, true));
    }

    @Override
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            return null;
        }

        // 检查访问权限（帖子必须是已审核的，或者当前用户是发帖人）
        boolean canAccess = post.getStatus() == 2 || // 已审核
                (currentUserId != null && post.getUserId().equals(currentUserId)); // 是发帖人

        if (!canAccess) {
            return null;
        }

        PostVO vo = convertToVO(post, currentUserId != null && post.getUserId().equals(currentUserId));

        // 增加浏览次数（异步或延迟更新，避免频繁更新影响性能）
        if (currentUserId == null || !post.getUserId().equals(currentUserId)) {
            incrementViewCount(postId);
        }

        return vo;
    }

    @Override
    public boolean updatePostStatus(Long postId, Byte status, Long adminUserId) {
        Post existing = postMapper.selectById(postId);
        if (existing == null || existing.getDeleted() == 1) {
            return false;
        }

        // 验证状态值
        if (status != 0 && status != 1 && status != 2) {
            throw new IllegalArgumentException("状态值必须是0、1或2");
        }

        Post update = new Post();
        update.setId(postId);
        update.setStatus(status);

        int result = postMapper.updateById(update);
        return result > 0;
    }

    @Override
    public boolean deletePost(Long id, Long currentUserId) {
        // 先查询帖子是否存在
        Post post = postMapper.selectById(id);
        if (post == null) {
            return false;
        }

        // 检查是否为当前用户发布的帖子
        if (!post.getUserId().equals(currentUserId)) {
            return false;
        }

        // 使用条件更新进行逻辑删除
        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, id)
                .eq(Post::getUserId, currentUserId) // 确保只能删除自己的帖子
                .set(Post::getDeleted, (byte) 1);   // 设置deleted为1，表示已删除

        int result = postMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    public boolean incrementViewCount(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            return false;
        }

        // 使用SQL直接更新，避免并发问题
        return postMapper.incrementViewCount(postId) > 0;
    }

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
            vo.setFormattedDate(java.time.LocalDateTime.now().toString());
        }

        // 获取用户昵称
        if (includePrivateInfo) {
            vo.setIsOwner(true);
        }

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