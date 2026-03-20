// top/yyf/psych_support/service/impl/UserServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.PageResponse;
import top.yyf.psych_support.model.UserResponse;
import top.yyf.psych_support.model.UserSearchRequest;
import top.yyf.psych_support.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUserByEmail(String email) {
        log.info("根据邮箱查询用户: {}", email);
        return this.baseMapper.selectByEmail(email);
    }

    @Override
    public UserResponse getUserDetail(Long userId) {
        log.info("获取用户详情，ID: {}", userId);
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            log.warn("用户不存在或已删除，ID: {}", userId);
            throw new RuntimeException("用户不存在");
        }
        return convertToResponse(user);
    }

    @Override
    public PageResponse<UserResponse> searchUsers(UserSearchRequest request) {
        log.info("搜索用户，关键词: {}, 角色: {}", request.getKeyword(), request.getRole());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(User::getEmail, request.getKeyword())
                    .or().like(User::getNickname, request.getKeyword()));
        }

        if (request.getRole() != null) {
            wrapper.eq(User::getRole, request.getRole());
        }

        if (request.getBanned() != null) {
            wrapper.eq(User::getBanned, request.getBanned());
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        this.page(page, wrapper);

        List<UserResponse> records = page.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                records
        );
    }

    @Override
    public List<UserResponse> getAllStudents() {
        log.info("获取所有学生");
        List<User> users = this.baseMapper.selectAllStudents();
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getAllCounselors() {
        log.info("获取所有咨询师");
        List<User> users = this.baseMapper.selectAllCounselors();
        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateBanStatus(Long userId, Boolean banned) {
        log.info("更新用户封禁状态，ID: {}, banned: {}", userId, banned);
        User user = this.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            log.error("用户不存在，ID: {}", userId);
            return false;
        }
        user.setBanned(banned);
        return this.updateById(user);
    }

    /**
     * 将 User 实体转换为 UserResponse
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        // 注意：这里假设你有 UserResponse 类，包含 getRoleName() 等计算字段
        // 可使用 BeanUtils.copyProperties，但需确保字段名匹配
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setRoleName(user.getRoleName()); // 来自 User.java 的 @JsonGetter
        response.setCollege(user.getCollege());
        response.setGrade(user.getGrade());
        response.setBanned(user.getIsBanned()); // 来自 User.java 的 @JsonGetter
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    public boolean isEmailExists(String email) {
        return this.baseMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, email)
                        .eq(User::getDeleted, 0)
        ) > 0;
    }
}