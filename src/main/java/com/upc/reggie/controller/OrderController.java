package com.upc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.reggie.common.R;
import com.upc.reggie.common.ThreadLocalUtils;
import com.upc.reggie.dto.OrderDto;
import com.upc.reggie.entity.*;
import com.upc.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/order")
@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Map<String, Object> map){
        //        Long addressBookId
        //        Integer payMethod
        //        String remark
        Long addressBookId = Long.valueOf((String)map.get("addressBookId")) ;
        Integer payMethod = (Integer) map.get("payMethod");
        String remark = (String) map.get("addressBookId");

        log.info("addressBookld:{}",addressBookId);
        log.info("payMethod:{}",payMethod);
        log.info("remark:{}",remark);
        Long currentId = ThreadLocalUtils.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> shoppingCartList =  shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            return R.error("购物车为空，不能下单");
        }
        //查询用户数据
        User user = userService.getById(currentId);
        //查询地址数据
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            return R.error("用户地址信息有误，不能下单");
        }
        long orderId = IdWorker.getId();//订单号
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        Orders orders = new Orders();
        orders.setAddressBookId(addressBookId);
        orders.setPayMethod(payMethod);
        orders.setRemark(remark);
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now().toString());
        orders.setCheckoutTime(LocalDateTime.now().toString());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        orderService.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);
        //清空购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(queryWrapper);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(Integer page,Integer pageSize){
        Page<OrderDto> dtoPage = new Page<>();
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId,ThreadLocalUtils.getCurrentId());
        orderService.page(ordersPage,lambdaQueryWrapper);
        List<Orders> ordersRecords = ordersPage.getRecords();
        BeanUtils.copyProperties(ordersPage, dtoPage,"records");
        List<OrderDto> orderDtoList = ordersRecords.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            Long itemId = item.getId();
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId, itemId);
            List<OrderDetail> list = orderDetailService.list(queryWrapper);
            orderDto.setOrderDetails(list);
            BeanUtils.copyProperties(item, orderDto);
            return orderDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(orderDtoList);
        return R.success(dtoPage);
    }

}
