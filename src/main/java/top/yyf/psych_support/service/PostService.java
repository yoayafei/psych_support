package top.yyf.psych_support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.yyf.psych_support.entity.Post;
import top.yyf.psych_support.model.vo.CreatePostVO;
import top.yyf.psych_support.model.vo.PostVO;

public interface PostService {
    /**
     * 创建帖子
     */
    Post createPost(CreatePostVO createPostVO, Long userId);

    /**
     * 获取帖子列表（分页）
     */
    Page<PostVO> getPosts(Integer page, Integer size, Byte status);

    /**
     * 获取用户发布的帖子列表
     */
    Page<PostVO> getUserPosts(Long userId, Integer page, Integer size);

    /**
     * 获取帖子详情
     */
    PostVO getPostDetail(Long postId, Long currentUserId);

    /**
     * 更新帖子状态
     */
    boolean updatePostStatus(Long postId, Byte status, Long adminUserId);

    /**
     * 删除帖子（逻辑删除）
     */
    boolean deletePost(Long postId, Long currentUserId);

    /**
     * 增加浏览次数
     */
    boolean incrementViewCount(Long postId);
}