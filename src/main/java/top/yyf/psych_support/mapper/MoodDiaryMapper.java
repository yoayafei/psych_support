package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yyf.psych_support.entity.MoodDiary;

@Mapper
public interface MoodDiaryMapper extends BaseMapper<MoodDiary> {
}