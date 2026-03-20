// top/yyf/psych_support/controller/UserController.java
package top.yyf.psych_support.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.model.*;
import top.yyf.psych_support.service.UserService;
import top.yyf.psych_support.util.JwtUtils;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.util.PasswordUtil;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordUtil passwordUtil;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<String> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册，邮箱: {}", request.getEmail());

        // 检查邮箱是否已存在
        if (userService.getUserByEmail(request.getEmail()) != null) {
            return Result.error("该邮箱已被注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordUtil.encode(request.getPassword())); // 🔒 加密密码
        user.setRole(0); // 默认学生
        user.setCollege(request.getCollege());
        user.setGrade(request.getGrade());
        user.setBanned(false);

        boolean saved = userService.save(user);
        if (!saved) {
            return Result.error("注册失败");
        }
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("用户登录，邮箱: {}", request.getEmail());

        User user = userService.getUserByEmail(request.getEmail());
        if (user == null) {
            return Result.error("邮箱或密码错误"); // 避免暴露“邮箱不存在”
        }
        if (user.getBanned() != null && user.getBanned()) {
            return Result.error("账号已被封禁");
        }

        // 🔑 校验密码
        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            return Result.error("邮箱或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId());
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        // 构建用户信息（不含密码）
        UserResponse userResp = new UserResponse();
        userResp.setId(user.getId());
        userResp.setEmail(user.getEmail());
        userResp.setNickname(user.getNickname());
        userResp.setRole(user.getRole());
        userResp.setRoleName(user.getRoleName());
        userResp.setAvatar(user.getAvatar());
        userResp.setCollege(user.getCollege());
        userResp.setGrade(user.getGrade());
        userResp.setBanned(user.getIsBanned());
        userResp.setCreatedAt(user.getCreatedAt());

        response.setUser(userResp);
        return Result.success(response);
    }

    /**
     * 获取当前用户信息（需登录）
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<UserResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        log.info("获取当前用户信息，ID: {}", userId);
        UserResponse user = userService.getUserDetail(userId);
        return Result.success(user);
    }

    /**
     * 获取用户详情（管理端，根据 ID）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        log.info("获取用户详情，ID: {}", id);
        UserResponse user = userService.getUserDetail(id);
        return Result.success(user);
    }

    /**
     * 搜索用户（管理端分页）
     */
    @PostMapping("/search")
    @Operation(summary = "搜索用户")
    public Result<PageResponse<UserResponse>> searchUsers(@RequestBody UserSearchRequest request) {
        log.info("搜索用户，请求: {}", request);
        PageResponse<UserResponse> result = userService.searchUsers(request);
        return Result.success(result);
    }

    /**
     * 更新用户封禁状态（管理端）
     */
    @PutMapping("/{id}/ban")
    @Operation(summary = "更新用户封禁状态")
    public Result<String> updateBanStatus(
            @PathVariable Long id,
            @RequestBody UpdateBanRequest request) {
        log.info("更新用户封禁状态，ID: {}, banned: {}", id, request.getBanned());
        boolean updated = userService.updateBanStatus(id, request.getBanned());
        if (!updated) {
            return Result.error("操作失败");
        }
        return Result.success("操作成功");
    }

    /**
     * 获取所有学生（用于预约选择等）
     */
    @GetMapping("/students")
    @Operation(summary = "获取所有学生")
    public Result<List<UserResponse>> getAllStudents() {
        log.info("获取所有学生");
        List<UserResponse> students = userService.getAllStudents();
        return Result.success(students);
    }

    /**
     * 获取所有咨询师（用于预约选择等）
     */
    @GetMapping("/counselors")
    @Operation(summary = "获取所有咨询师")
    public Result<List<UserResponse>> getAllCounselors() {
        log.info("获取所有咨询师");
        List<UserResponse> counselors = userService.getAllCounselors();
        return Result.success(counselors);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出账号")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info("用户请求登出，Token 前缀: {}", token.substring(0, Math.min(10, token.length())));

        // 将 token 加入黑名单
        jwtUtils.addToBlacklist(token);

        // 可选：清除前端的 token（由前端自己处理）
        return Result.success("登出成功");
    }
}