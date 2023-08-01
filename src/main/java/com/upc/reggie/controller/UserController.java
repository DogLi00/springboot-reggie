package com.upc.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.reggie.common.R;
import com.upc.reggie.common.ThreadLocalUtils;
import com.upc.reggie.entity.User;
import com.upc.reggie.service.UserService;
import com.upc.reggie.utils.SMSUtils;
import com.upc.reggie.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
@Api("用户接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){
        String phone = user.getPhone();
        log.info("phone:{}",phone);
        Integer code = ValidateCodeUtils.generateValidateCode(4);
        log.info("code:{}",code);
        redisTemplate.opsForValue().set("code", code,5, TimeUnit.MINUTES);
//        try {
//            SMSUtils.sendMessage(phone,code.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        log.info("sendMessage成功");
        return R.success("验证码发送成功");
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody Map map, HttpSession session){
        String phone = (String) map.get("phone");
        Integer code = Integer.parseInt((String) map.get("code"));
        Integer codeTrue = (Integer) redisTemplate.opsForValue().get("code");
        if (!code.equals(codeTrue)){
            return R.error("登陆失败");
        }
        User user = null;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);
        user = userService.getOne(queryWrapper);
        if (user == null){
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
            LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(User::getPhone,phone);
            user = userService.getOne(queryWrapper1);
        }
        session.setAttribute("user", user.getId());
        return R.success("登陆成功");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
