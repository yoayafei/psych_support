package top.yyf.psych_support.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.entity.MoodDiary;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.dto.CreateMoodDiaryDTO;
import top.yyf.psych_support.model.dto.UpdateMoodDiaryDTO;
import top.yyf.psych_support.model.vo.MoodDiaryVO;
import top.yyf.psych_support.service.MoodDiaryService;
import top.yyf.psych_support.util.JwtUtils;

import java.time.LocalDateTime;

@Slf4j
@Tag(name = "情绪日记管理", description = "情绪日记的增删改查及相关操作")
@RestController
@RequestMapping("/api/mood-diary")
@RequiredArgsConstructor
public class MoodDiaryController {

    private final MoodDiaryService moodDiaryService;

    @Autowired
    private JwtUtils jwtUtils; // 注入你的JWT工具类

    @Autowired
    private UserMapper userMapper; // 注入UserMapper用于获取用户信息

    @Operation(summary = "创建情绪日记")
    @PostMapping
    public Result<MoodDiaryVO> createMoodDiary(@Valid @RequestBody CreateMoodDiaryDTO createDTO,
                                               @RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);

            // 构建实体对象
            MoodDiary moodDiary = new MoodDiary();
            moodDiary.setContent(createDTO.getContent());
            moodDiary.setMoodTag(createDTO.getMoodTag());
            moodDiary.setMoodLevel(createDTO.getMoodLevel());
            moodDiary.setImageUrl(createDTO.getImageUrl());
            moodDiary.setIsPublic(createDTO.getIsPublic());
            moodDiary.setUserId(userId); // 从token获取，不由前端传递

            MoodDiary created = moodDiaryService.createMoodDiary(moodDiary);

            // 转换为VO返回
            MoodDiaryVO vo = convertToVO(created);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("创建情绪日记失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的情绪日记列表")
    @GetMapping("/my")
    public Result<Page<MoodDiaryVO>> getMyMoodDiaries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            Page<MoodDiaryVO> result = moodDiaryService.getUserMoodDiaries(userId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取我的情绪日记列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取公开的情绪日记列表")
    @GetMapping("/public")
    public Result<Page<MoodDiaryVO>> getPublicMoodDiaries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Page<MoodDiaryVO> result = moodDiaryService.getPublicMoodDiaries(page, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取公开情绪日记列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取情绪日记详情")
    @GetMapping("/{id}")
    public Result<MoodDiaryVO> getMoodDiaryDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            MoodDiaryVO result = moodDiaryService.getMoodDiaryDetail(id, userId);

            if (result == null) {
                return Result.error("日记不存在或无权访问");
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取情绪日记详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新情绪日记")
    @PutMapping("/{id}")
    public Result<Boolean> updateMoodDiary(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMoodDiaryDTO updateDTO,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);

            // 构建要更新的对象
            MoodDiary moodDiary = new MoodDiary();
            moodDiary.setContent(updateDTO.getContent());
            moodDiary.setMoodTag(updateDTO.getMoodTag());
            moodDiary.setMoodLevel(updateDTO.getMoodLevel());
            moodDiary.setImageUrl(updateDTO.getImageUrl());
            moodDiary.setIsPublic(updateDTO.getIsPublic());

            boolean result = moodDiaryService.updateMoodDiary(id, moodDiary, userId);

            if (!result) {
                return Result.error("更新失败或无权操作");
            }

            return Result.success(true);
        } catch (Exception e) {
            log.error("更新情绪日记失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除情绪日记")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMoodDiary(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            boolean result = moodDiaryService.deleteMoodDiary(id, userId);

            if (!result) {
                return Result.error("删除失败或无权操作");
            }

            return Result.success(true);
        } catch (Exception e) {
            log.error("删除情绪日记失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "设置日记公开状态")
    @PatchMapping("/{id}/public-status")
    public Result<Boolean> setPublicStatus(
            @PathVariable Long id,
            @RequestParam Byte isPublic,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            boolean result = moodDiaryService.setPublicStatus(id, isPublic, userId);

            if (!result) {
                return Result.error("设置失败或无权操作");
            }

            return Result.success(true);
        } catch (Exception e) {
            log.error("设置公开状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    // 辅助方法：转换为VO
    private MoodDiaryVO convertToVO(MoodDiary diary) {
        MoodDiaryVO vo = new MoodDiaryVO();
        vo.setId(diary.getId());
        vo.setUserId(diary.getUserId());
        vo.setContent(diary.getContent());
        vo.setMoodTag(diary.getMoodTag());
        vo.setMoodLevel(diary.getMoodLevel());
        vo.setImageUrl(diary.getImageUrl());
        vo.setIsPublic(diary.getIsPublic());
        vo.setCreatedAt(diary.getCreatedAt());

        // 安全地处理createdAt为null的情况
        if (diary.getCreatedAt() != null) {
            vo.setFormattedDate(diary.getCreatedAt().toString());
        } else {
            vo.setFormattedDate(LocalDateTime.now().toString());
        }

        // 获取用户昵称
        User user = userMapper.selectById(diary.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname());
        }

        return vo;
    }
}