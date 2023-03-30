package ren.lawliet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import ren.lawliet.entity.ResponseEntity;
import ren.lawliet.entity.UserEntity;
import ren.lawliet.entity.regUser;
import ren.lawliet.mapper.UserMapper;
import ren.lawliet.util.Helper;
import ren.lawliet.util.JwtUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    final
    UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

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
    ResponseEntity login(String lid,String password){
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setCode(200);
        password = Helper.md5(password);
        var userData = lid.split("#");
        String nickname = userData[0];
        String number = userData[userData.length-1];
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname",nickname).and(qw -> qw.eq("number",number));
        UserEntity userEntity = userMapper.selectOne(queryWrapper);
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
    ResponseEntity getUser(@PathVariable("uuid") String id){
        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setCode(200);
        UserEntity userEntity = userMapper.selectById(id);
        HashMap<String,String> data = new HashMap<>();
        if (userEntity != null){
            data.put("nickname", userEntity.getNickname());
            data.put("uuid",userEntity.getUuid());
            data.put("number", String.valueOf(userEntity.getNumber()));
            responseEntity.setMessage(data);
        }else{
            responseEntity.setCode(201);
        }
        return responseEntity;
    }
    //创建新用户 账号密码
    @PostMapping("/insert")
    ResponseEntity insertUser(@RequestBody regUser regUser){
        String password = regUser.getPassword();
        String nickname = regUser.getNickname();
        UserEntity userEntity = new UserEntity();
        password = Helper.md5(password);
        userEntity.setNickname(nickname);
        userEntity.setPassword(password);

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname",nickname);
        if (userMapper.selectCount(queryWrapper) == 0){
            userEntity.setNumber(0);
        }else{
            var users = userMapper.selectList(queryWrapper);
            ArrayList<Integer> numbers = new ArrayList<>();
            for (var user : users){
                numbers.add(user.getNumber());
            }
            int maxNum = Collections.max(numbers);
            userEntity.setNumber(maxNum+1);
        }
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