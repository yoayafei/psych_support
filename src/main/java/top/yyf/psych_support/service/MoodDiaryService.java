package top.yyf.psych_support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.yyf.psych_support.entity.MoodDiary;
import top.yyf.psych_support.model.vo.MoodDiaryVO;

public interface MoodDiaryService extends IService<MoodDiary> {
    /**
     * 创建情绪日记
     */
    MoodDiary createMoodDiary(MoodDiary moodDiary);

    /**
     * 获取用户的情绪日记列表
     */
    Page<MoodDiaryVO> getUserMoodDiaries(Long userId, Integer page, Integer size);

    /**
     * 获取公开的情绪日记列表（供其他用户查看）
     */
    Page<MoodDiaryVO> getPublicMoodDiaries(Integer page, Integer size);

    /**
     * 获取单个情绪日记详情
     */
    MoodDiaryVO getMoodDiaryDetail(Long diaryId, Long currentUserId);

    /**
     * 更新情绪日记
     */
    boolean updateMoodDiary(Long diaryId, MoodDiary moodDiary, Long currentUserId);

    /**
     * 删除情绪日记
     */
    boolean deleteMoodDiary(Long diaryId, Long currentUserId);

    /**
     * 设置日记公开状态
     */
    boolean setPublicStatus(Long diaryId, Byte isPublic, Long currentUserId);
}
