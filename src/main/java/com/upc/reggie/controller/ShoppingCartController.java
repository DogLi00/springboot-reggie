package com.upc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.upc.reggie.common.R;
import com.upc.reggie.common.ThreadLocalUtils;
import com.upc.reggie.entity.ShoppingCart;
import com.upc.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long userId = ThreadLocalUtils.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody ShoppingCart shoppingCart){
        shoppingCart.setUserId(ThreadLocalUtils.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        if (dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);

        }
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
        if (shoppingCart1!=null){
            shoppingCart1.setNumber(shoppingCart1.getNumber()+1);
            shoppingCartService.updateById(shoppingCart1);
        }else{
            shoppingCartService.save(shoppingCart);
        }
        return R.success("添加成功");
    }

    @RequestMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        shoppingCart.setUserId(ThreadLocalUtils.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        if (dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
        log.info("shoppingCart1.getNumber():{}",shoppingCart1.getNumber());
        if (shoppingCart1.getNumber()==1){
            shoppingCartService.removeById(shoppingCart1);
        }else{
            shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
            shoppingCartService.updateById(shoppingCart1);
        }
        return R.success("添加成功");
    }

    @DeleteMapping("clean")
    public R<String> clean(){
        Long currentId = ThreadLocalUtils.getCurrentId();
        if (currentId!=null){
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,currentId);
            shoppingCartService.remove(queryWrapper);
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

}
