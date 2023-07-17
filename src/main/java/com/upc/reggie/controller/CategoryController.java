package com.upc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.reggie.common.R;
import com.upc.reggie.entity.Category;
import com.upc.reggie.entity.Employee;
import com.upc.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> queryCategories(int page,int pageSize){
        log.info("page = {},pageSize = {}" ,page,pageSize);
        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Category::getUpdateTime);
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping
    public R<String> addCategory(@RequestBody Category category){
        log.info(category.toString());
        categoryService.save(category);
        return R.success("菜品添加成功");
    }

    @PutMapping
    public R<String> updateCategory(@RequestBody Category category){
        log.info(category.toString());
        categoryService.updateById(category);
        return R.success("菜品修改成功");
    }

    @DeleteMapping
    public R<String> deleteCategory(@RequestParam("ids") Long id){
        Integer remove = categoryService.remove(id);
        if (remove==0){
            return R.error("无法删除，已关联套餐或菜品");
        }
        else {
            return R.success("删除成功");
        }
    }

    @GetMapping("/list")
    public R<List<Category>> list(String type){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type!=null,Category::getType,type);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
