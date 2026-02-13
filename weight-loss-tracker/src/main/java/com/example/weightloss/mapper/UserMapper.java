package com.example.weightloss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weightloss.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus已提供基础CRUD方法
    // 如需自定义查询可在此添加
}