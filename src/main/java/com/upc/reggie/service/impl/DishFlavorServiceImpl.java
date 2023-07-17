package com.upc.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.reggie.entity.DishFlavor;
import com.upc.reggie.mapper.DishFlavorMapper;
import com.upc.reggie.service.DishFlavorService;
import com.upc.reggie.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService  {
}
