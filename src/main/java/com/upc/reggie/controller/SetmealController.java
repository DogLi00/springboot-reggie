package com.upc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.reggie.common.R;
import com.upc.reggie.dto.DishDto;
import com.upc.reggie.dto.SetmealDto;
import com.upc.reggie.entity.Category;
import com.upc.reggie.entity.Dish;
import com.upc.reggie.entity.Setmeal;
import com.upc.reggie.entity.SetmealDish;
import com.upc.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;
    @PostMapping
    public R<String> add(@RequestBody SetmealDto setmealDto){
        log.info("数据显示------------------"+setmealDto.toString());
        setmealService.save(setmealDto);
        List<SetmealDish> setmealDisheList = setmealDto.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDisheList.stream().map((item) -> {
            SetmealDish setmealDish = new SetmealDish();
            BeanUtils.copyProperties(item, setmealDish);
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> show(Integer page, Integer pageSize, String name){
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(name), Setmeal::getName,name);
        setmealService.page(setmealPage, queryWrapper);
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> dtos = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;

        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(dtos);
        return R.success(setmealDtoPage);
    }

    @GetMapping("/{setmealId}")
    public R<SetmealDto> show(@PathVariable Long setmealId){
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = setmealService.getById(setmealId);
        BeanUtils.copyProperties(setmeal, setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> saveChange(@RequestBody SetmealDto setmealDto){
        setmealService.removeById(setmealDto.getId());
        setmealService.save(setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        List<SetmealDish> setmealDisheList = setmealDto.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDisheList.stream().map((item) -> {
            SetmealDish setmealDish = new SetmealDish();
            BeanUtils.copyProperties(item, setmealDish);
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
        return R.success("修改成功");
    }

    @DeleteMapping()
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = setmealService.count(queryWrapper);
        if (count>0){
            return R.error("套餐正在售卖,无法删除");
        }
        for(Long id : ids){
            Setmeal setmeal = setmealService.getById(id);
            File file = new File(basePath+setmeal.getImage());
            file.delete();
            setmealService.removeById(id);
            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(SetmealDish::getSetmealId, id);
            setmealDishService.remove(queryWrapper1);
        }
        return R.success("删除成功");
    }

    @PostMapping("/status/{statusValue}")
    public R<String> changeStatus(@PathVariable("statusValue") Integer status,@RequestParam("ids") List<Long> ids){
        for (Long id : ids){
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Setmeal::getId, id).set(Setmeal::getStatus, status);
            setmealService.update(updateWrapper);
        }
        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R<List<SetmealDto>> listSetmeal(@RequestParam("categoryId") Long categoryId, @RequestParam("status")Integer status){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(status!=null,Setmeal::getStatus,status);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(SetmealDish::getSetmealId,item.getId());
            List<SetmealDish> list = setmealDishService.list(queryWrapper1);
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName() );
            setmealDto.setSetmealDishes(list);

            return setmealDto;
        }).collect(Collectors.toList());

        return R.success(setmealDtoList);
    }

    @GetMapping("/dish/{id}")
    public R<List<Dish>> listSetmealDto(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<Dish> dishes = list.stream().map((item) -> {
            Long dishId = item.getDishId();
            return dishService.getById(dishId);
        }).collect(Collectors.toList());
        return R.success(dishes);
    }


}
