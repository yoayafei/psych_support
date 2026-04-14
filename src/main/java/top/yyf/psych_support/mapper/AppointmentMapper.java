// 文件路径: top.yyf.psych_support.mapper.AppointmentMapper.java
package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yyf.psych_support.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 查询用户的预约记录（分页）
     */
    @Select("SELECT * FROM appointments WHERE user_id = #{userId} ORDER BY created_at DESC")
    IPage<Appointment> selectMyAppointments(Page<Appointment> page, @Param("userId") Long userId);

    /**
     * 查询咨询师的预约记录（分页）
     */
    @Select("SELECT * FROM appointments WHERE counselor_id = #{counselorId} ORDER BY created_at DESC")
    IPage<Appointment> selectCounselorAppointments(Page<Appointment> page, @Param("counselorId") Long counselorId);

    /**
     * 查询某个时间段的预约记录数量（用于判断是否已满）
     */
    @Select("SELECT COUNT(*) FROM appointments WHERE slot_id = #{slotId} AND status IN ('PENDING', 'CONFIRMED')")
    int countBySlotId(@Param("slotId") Long slotId);

    /**
     * 查询用户在某个时间段是否有未完成的预约（防止重复预约）
     */
    @Select("SELECT COUNT(*) FROM appointments WHERE user_id = #{userId} AND slot_id = #{slotId} AND status IN ('PENDING', 'CONFIRMED')")
    int countByUserAndSlot(@Param("userId") Long userId, @Param("slotId") Long slotId);

    /**
     * 查询用户在指定日期范围内的预约（用于限制预约频率）
     */
    @Select("SELECT COUNT(*) FROM appointments WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate} AND status IN ('PENDING', 'CONFIRMED')")
    int countByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询当天的预约记录（用于咨询师日程）
     */
    @Select("SELECT * FROM appointments WHERE counselor_id = #{counselorId} AND date = #{date} AND status IN ('CONFIRMED', 'COMPLETED') ORDER BY start_time ASC")
    List<Appointment> selectByCounselorAndDate(@Param("counselorId") Long counselorId, @Param("date") LocalDate date);

    /**
     * 查询即将开始的预约（用于提醒）
     */
    @Select("SELECT * FROM appointments WHERE status = 'CONFIRMED' AND start_time BETWEEN #{startTime} AND #{endTime}")
    List<Appointment> selectUpcomingAppointments(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM appointments WHERE counselor_id = #{counselorId} AND date = #{date} AND slot_id = #{slotId} AND status IN ('PENDING', 'CONFIRMED')")
    int countByCounselorDateAndSlot(@Param("counselorId") Long counselorId,
                                    @Param("date") LocalDate date,
                                    @Param("slotId") Long slotId);

    @Select("SELECT COUNT(*) FROM appointments WHERE user_id = #{userId} AND slot_id = #{slotId} AND date = #{date} AND status IN ('PENDING', 'CONFIRMED')")
    int countByUserAndSlotAndDate(@Param("userId") Long userId,
                                  @Param("slotId") Long slotId,
                                  @Param("date") LocalDate date);


    @Select("SELECT * FROM appointments WHERE counselor_id = #{counselorId} AND date = #{date} AND status IN ('PENDING', 'CONFIRMED')")
    List<Appointment> findBookedByCounselorAndDate(@Param("counselorId") Long counselorId, @Param("date") LocalDate date);
}