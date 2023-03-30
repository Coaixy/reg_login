package ren.lawliet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ren.lawliet.entity.ResponseEntity;
import ren.lawliet.entity.UserEntity;
import ren.lawliet.mapper.UserMapper;
import ren.lawliet.util.Helper;
import ren.lawliet.util.JwtUtil;

import java.lang.annotation.Repeatable;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserMapper userMapper;

    /**
     * RestFul 风格
     * GET（SELECT）：从服务器取出资源（一项或多项）。
     * POST（CREATE）：在服务器新建一个资源。
     * PUT（UPDATE）：在服务器更新资源（客户端提供完整资源数据）。
     * PATCH（UPDATE）：在服务器更新资源（客户端提供需要修改的资源数据）。
     * DELETE（DELETE）：从服务器删除资源。
     */

    //登录
    @GetMapping("/login")
    ResponseEntity login(String uuid,String password){
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setCode(200);
        password = Helper.md5(password);
        UserEntity userEntity = userMapper.selectById(uuid);
        //如果没查到数据
        if (userEntity == null){
            responseEntity.setCode(201);
            responseEntity.setMessage("用户不存在");
        }else{
            if (userEntity.getPassword().equals(password)){
                String token = JwtUtil.generateJwtToken(userEntity.getUuid());
                responseEntity.setMessage(token);
            }else{
                responseEntity.setCode(201);
                responseEntity.setMessage("密码错误");
            }
        }
        return responseEntity;
    }
    //通过UUID获取NickName
    @GetMapping("/{uuid}")
    ResponseEntity getUser(@PathVariable("uuid") String id, HttpServletResponse response){
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setCode(200);
        UserEntity userEntity = userMapper.selectById(id);
        HashMap<String,String> data = new HashMap<>();
        if (userEntity != null){
            data.put("nickname", userEntity.getNickname());
            data.put("uuid",userEntity.getUuid());
            responseEntity.setMessage(data);
        }else{
            responseEntity.setCode(500);
        }
        return responseEntity;
    }
    //创建新用户 账号密码
    @PostMapping("/insert")
    ResponseEntity insertUser(@RequestBody UserEntity userEntity,HttpServletResponse response){
        String password = Helper.md5(userEntity.getPassword());
        userEntity.setPassword(password);
        int flag = userMapper.insert(userEntity);
        ResponseEntity responseEntity = new ResponseEntity();
        if (flag > 0){
            responseEntity.setCode(200);
            String token = JwtUtil.generateJwtToken(userEntity.getUuid());
            responseEntity.setMessage(token);
        }else{
            responseEntity.setCode(201);
            responseEntity.setMessage("异常");
        }
        return responseEntity;
    }
    //验证Token是否过期
    @GetMapping("/verity/{token}")
    ResponseEntity verityToken(@PathVariable("token")String token){
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setCode(200);
        try{
            Claims claims = JwtUtil.getClaimsFromJwt(token);
            var now = new Date().getTime();
            var exp = claims.getExpiration().getTime();
            if (exp <= now){
                responseEntity.setCode(201);
                responseEntity.setMessage("验证过期");
            }else{
                responseEntity.setMessage(claims.get("uuid"));
            }
        }catch (Exception e){
            responseEntity.setCode(201);
            responseEntity.setMessage("token检验错误");
        }
        return responseEntity;
    }
}
