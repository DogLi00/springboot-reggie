package com.upc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.reggie.dto.DishDto;
import com.upc.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    public void updateWithFlavor(DishDto dishDto);
    public DishDto getByIdWithFlavor(Long id);
}
