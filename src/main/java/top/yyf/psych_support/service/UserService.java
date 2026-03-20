// top/yyf/psych_support/service/UserService.java
package top.yyf.psych_support.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.model.UserResponse;
import top.yyf.psych_support.model.PageResponse;
import top.yyf.psych_support.model.UserSearchRequest;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 根据邮箱获取用户（登录用）
     */
    User getUserByEmail(String email);

    /**
     * 获取用户详情（含角色名等计算字段）
     */
    UserResponse getUserDetail(Long userId);

    /**
     * 搜索用户（管理端）
     */
    PageResponse<UserResponse> searchUsers(UserSearchRequest request);

    /**
     * 获取所有学生（简化版，不分页）
     */
    List<UserResponse> getAllStudents();

    /**
     * 获取所有咨询师
     */
    List<UserResponse> getAllCounselors();

    /**
     * 更新用户封禁状态
     */
    boolean updateBanStatus(Long userId, Boolean banned);

    boolean isEmailExists(String email);
}