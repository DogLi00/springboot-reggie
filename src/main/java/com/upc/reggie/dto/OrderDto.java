package com.upc.reggie.dto;

import com.upc.reggie.entity.OrderDetail;
import com.upc.reggie.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {
    private List<OrderDetail> orderDetails;
}
