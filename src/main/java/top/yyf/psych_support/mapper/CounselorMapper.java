// 文件路径: top.yyf.psych_support.mapper.CounselorMapper.java
package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yyf.psych_support.entity.Counselor;

import java.util.List;

@Mapper
public interface CounselorMapper extends BaseMapper<Counselor> {

    /**
     * 查询所有可用的咨询师
     */
    @Select("SELECT * FROM counselors WHERE is_available = TRUE")
    List<Counselor> selectAllAvailable();

    /**
     * 根据用户ID查询咨询师信息
     */
    @Select("SELECT * FROM counselors WHERE user_id = #{userId}")
    Counselor selectByUserId(@Param("userId") Long userId);
}