package com.upc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.reggie.common.R;
import com.upc.reggie.dto.DishDto;
import com.upc.reggie.entity.Category;
import com.upc.reggie.entity.Dish;
import com.upc.reggie.entity.DishFlavor;
import com.upc.reggie.service.CategoryService;
import com.upc.reggie.service.DishFlavorService;
import com.upc.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("dish")
public class DishController {
    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);
        //构造分页构造器对象
        Page pageInfo = new Page(page,pageSize);
        Page dishDtoPage = new Page();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.hasText(name),Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam("ids")List<Long> idList){
        for(Long ids : idList){
            Dish dish = dishService.getById(ids);
            String imageName = null;
            Long categoryId = null;
            if (dish!=null){
                categoryId = dish.getCategoryId();
                imageName = dish.getImage();
            }
            if (categoryId!=null){
                dishService.removeById(ids);
                categoryService.removeById(categoryId);
                File file = new File(basePath+imageName);
                file.delete();
            }

        }
//        System.out.println(ids);
        return R.success("删除成功");
    }

    @PostMapping("/status/{statusValue}")
    public R<String> changeStatus (@RequestParam("ids")  List<Long> idList,@PathVariable Integer statusValue){
        for (Long ids : idList){
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Dish::getStatus,statusValue).eq(Dish::getId, ids);
            dishService.update(updateWrapper);
        }
        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> listDish(Dish dish){
        List<DishDto> dtos = null;
        String key = "dish_" + dish.getCategoryId() + "_" +dish.getStatus();
        dtos = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dtos!=null){
            return R.success(dtos);
        }
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        dtos = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> list1 = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(list1);
            return dishDto;
        }).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key, dtos,60, TimeUnit.MINUTES);
        return R.success(dtos);
    }
}
