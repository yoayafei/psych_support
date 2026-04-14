package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import top.yyf.psych_support.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId} AND deleted = 0")
    int incrementViewCount(Long postId);
}