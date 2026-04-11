package top.yyf.psych_support.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");

        // 自动填充创建时间 - 使用实体类字段名 createdAt
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        log.info("填充createdAt: {}", LocalDateTime.now());

        // 自动填充更新时间 - 使用实体类字段名 updatedAt
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        log.info("填充updatedAt: {}", LocalDateTime.now());

        if (metaObject.hasSetter("deleted")) {
            Object deleted = getFieldValByName("deleted", metaObject);
            if (deleted == null) {
                setFieldValByName("deleted", (byte) 0, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");

        // 自动填充更新时间 - 使用实体类字段名 updatedAt
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        log.info("更新updatedAt: {}", LocalDateTime.now());
    }
}