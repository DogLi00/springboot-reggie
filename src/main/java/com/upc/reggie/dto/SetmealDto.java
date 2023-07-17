package com.upc.reggie.dto;

import com.upc.reggie.entity.Setmeal;
import com.upc.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
