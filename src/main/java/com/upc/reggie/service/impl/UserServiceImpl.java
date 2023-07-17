package com.upc.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.reggie.entity.User;
import com.upc.reggie.mapper.UserMapper;
import com.upc.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper, User>  implements UserService{
}
