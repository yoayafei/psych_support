// top/yyf/psych_support/mapper/UserMapper.java
package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.yyf.psych_support.entity.User;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据邮箱查询用户（用于登录）
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(String email);

    /**
     * 获取所有活跃学生（用于管理端）
     */
    @Select("SELECT * FROM user WHERE role = 0 AND is_banned = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<User> selectAllStudents();

    /**
     * 获取所有咨询师
     */
    @Select("SELECT * FROM user WHERE role = 1 AND is_banned = 0 AND deleted = 0")
    List<User> selectAllCounselors();
}