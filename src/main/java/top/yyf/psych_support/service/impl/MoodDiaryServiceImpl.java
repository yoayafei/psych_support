package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.yyf.psych_support.entity.MoodDiary;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.mapper.MoodDiaryMapper;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.vo.MoodDiaryVO;
import top.yyf.psych_support.service.MoodDiaryService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoodDiaryServiceImpl extends ServiceImpl<MoodDiaryMapper, MoodDiary> implements MoodDiaryService {

    private final MoodDiaryMapper moodDiaryMapper;
    private final UserMapper userMapper;

    @Override
    public MoodDiary createMoodDiary(MoodDiary moodDiary) {
        // 验证情绪等级范围
        if (moodDiary.getMoodLevel() < 1 || moodDiary.getMoodLevel() > 5) {
            throw new IllegalArgumentException("情绪强度必须在1-5之间");
        }

        // 设置默认值
        if (moodDiary.getIsPublic() == null) {
            moodDiary.setIsPublic((byte) 0); // 默认私密
        }

        moodDiaryMapper.insert(moodDiary);
        return moodDiary;
    }

    @Override
    public Page<MoodDiaryVO> getUserMoodDiaries(Long userId, Integer page, Integer size) {
        Page<MoodDiary> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<MoodDiary> wrapper = new LambdaQueryWrapper<MoodDiary>()
                .eq(MoodDiary::getUserId, userId)
                .orderByDesc(MoodDiary::getCreatedAt);

        Page<MoodDiary> result = moodDiaryMapper.selectPage(mpPage, wrapper);

        return (Page<MoodDiaryVO>) result.convert(diary -> convertToVO(diary, true)); // 包含私密日记
    }

    @Override
    public Page<MoodDiaryVO> getPublicMoodDiaries(Integer page, Integer size) {
        Page<MoodDiary> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<MoodDiary> wrapper = new LambdaQueryWrapper<MoodDiary>()
                .eq(MoodDiary::getIsPublic, (byte) 2) // 只查询已公开的
                .orderByDesc(MoodDiary::getCreatedAt);

        Page<MoodDiary> result = moodDiaryMapper.selectPage(mpPage, wrapper);

        return (Page<MoodDiaryVO>) result.convert(diary -> convertToVO(diary, false)); // 作为公开日记处理
    }

    @Override
    public MoodDiaryVO getMoodDiaryDetail(Long diaryId, Long currentUserId) {
        MoodDiary diary = moodDiaryMapper.selectById(diaryId);
        if (diary == null) {
            return null;
        }

        // 检查访问权限
        boolean canAccess = diary.getUserId().equals(currentUserId) ||
                diary.getIsPublic() == 2; // 自己的日记或已公开的日记

        if (!canAccess) {
            return null;
        }

        return convertToVO(diary, diary.getUserId().equals(currentUserId));
    }

    @Override
    public boolean updateMoodDiary(Long diaryId, MoodDiary moodDiary, Long currentUserId) {
        MoodDiary existing = moodDiaryMapper.selectById(diaryId);
        if (existing == null || !existing.getUserId().equals(currentUserId)) {
            return false;
        }

        // 验证情绪等级范围
        if (moodDiary.getMoodLevel() != null &&
                (moodDiary.getMoodLevel() < 1 || moodDiary.getMoodLevel() > 5)) {
            throw new IllegalArgumentException("情绪强度必须在1-5之间");
        }

        moodDiary.setId(diaryId);
        moodDiary.setUserId(existing.getUserId()); // 保持原始用户ID不变

        int result = moodDiaryMapper.updateById(moodDiary);
        return result > 0;
    }

    @Override
    public boolean deleteMoodDiary(Long diaryId, Long currentUserId) {
        // 先验证日记是否存在且属于当前用户
        LambdaQueryWrapper<MoodDiary> checkWrapper = new LambdaQueryWrapper<MoodDiary>()
                .eq(MoodDiary::getId, diaryId)
                .eq(MoodDiary::getUserId, currentUserId)
                .eq(MoodDiary::getDeleted, (byte) 0); // 确保未被删除

        MoodDiary existing = moodDiaryMapper.selectOne(checkWrapper);
        if (existing == null) {
            log.warn("尝试删除不存在或不属于当前用户的日记: diaryId={}, userId={}", diaryId, currentUserId);
            return false;
        }

        // 使用MyBatis-Plus的逻辑删除功能
        // 注意：这里我们使用逻辑删除，而不是手动更新deleted字段
        int result = baseMapper.deleteById(diaryId);

        log.info("逻辑删除结果: diaryId={}, userId={}, affectedRows={}", diaryId, currentUserId, result);
        return result > 0;
    }

    @Override
    public boolean setPublicStatus(Long diaryId, Byte isPublic, Long currentUserId) {
        MoodDiary existing = moodDiaryMapper.selectById(diaryId);
        if (existing == null || !existing.getUserId().equals(currentUserId)) {
            return false;
        }

        // 验证公开状态值
        if (isPublic != 0 && isPublic != 1 && isPublic != 2) {
            throw new IllegalArgumentException("公开状态必须是0、1或2");
        }

        MoodDiary update = new MoodDiary();
        update.setId(diaryId);
        update.setIsPublic(isPublic);

        int result = moodDiaryMapper.updateById(update);
        return result > 0;
    }

    private MoodDiaryVO convertToVO(MoodDiary diary, boolean includePrivateInfo) {
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

        // 只有在允许的情况下才显示用户昵称
        if (includePrivateInfo || diary.getIsPublic() == 2) {
            User user = userMapper.selectById(diary.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname()); // 修改为nickname
            }
        }

        return vo;
    }
}