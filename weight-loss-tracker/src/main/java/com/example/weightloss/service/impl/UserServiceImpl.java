package com.example.weightloss.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weightloss.entity.User;
import com.example.weightloss.mapper.UserMapper;
import com.example.weightloss.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 可以在这里添加自定义业务逻辑
}