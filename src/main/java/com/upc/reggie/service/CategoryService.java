package com.upc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    Integer remove(Long id);
}
