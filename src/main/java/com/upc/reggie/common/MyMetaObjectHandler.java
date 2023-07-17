package com.upc.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        setFieldValByName("createTime", LocalDateTime.now().toString(), metaObject);
        setFieldValByName("updateTime", LocalDateTime.now().toString(), metaObject);
        setFieldValByName("createUser", ThreadLocalUtils.getCurrentId(), metaObject);
        setFieldValByName("updateUser", ThreadLocalUtils.getCurrentId(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", LocalDateTime.now().toString(), metaObject);
        setFieldValByName("updateUser", ThreadLocalUtils.getCurrentId(), metaObject);

    }
}
