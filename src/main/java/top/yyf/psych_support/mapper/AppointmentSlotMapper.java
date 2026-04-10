// 文件路径: top.yyf.psych_support.mapper.AppointmentSlotMapper.java
package top.yyf.psych_support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.yyf.psych_support.entity.AppointmentSlot;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AppointmentSlotMapper extends BaseMapper<AppointmentSlot> {

    /**
     * 查询咨询师某一天的所有时间段
     */
    @Select("SELECT * FROM appointment_slots WHERE counselor_id = #{counselorId} AND date = #{date} ORDER BY start_time ASC")
    List<AppointmentSlot> selectByCounselorAndDate(@Param("counselorId") Long counselorId, @Param("date") LocalDate date);

    /**
     * 查询咨询师某一天的可用时间段
     */
    @Select("SELECT * FROM appointment_slots WHERE counselor_id = #{counselorId} AND date = #{date} AND status = 1 ORDER BY start_time ASC")
//    List<AppointmentSlot> selectAvailableByCounselorAndDate(@Param("counselorId") Long counselorId, @Param("date") LocalDate date);
    List<AppointmentSlot> selectAvailableByCounselorAndDate(@Param("counselorId") Long counselorId);

    /**
     * 更新时间段状态（用于预约锁定）
     */
    @Update("UPDATE appointment_slots SET status = 2 WHERE id = #{slotId} AND status = 1")
    int lockSlot(@Param("slotId") Long slotId);
}