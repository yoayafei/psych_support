// 文件路径: top.yyf.psych_support.mapper.AppointmentFeedbackMapper.java
package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yyf.psych_support.entity.AppointmentFeedback;

@Mapper
public interface AppointmentFeedbackMapper extends BaseMapper<AppointmentFeedback> {

    /**
     * 根据预约ID查询评价
     */
    @Select("SELECT * FROM appointment_feedbacks WHERE appointment_id = #{appointmentId}")
    AppointmentFeedback selectByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 根据预约ID删除评价（当预约被删除时）
     */
    // MyBatis-Plus 自带 deleteByMap 或在 Service 层调用 removeByAppointmentId
}