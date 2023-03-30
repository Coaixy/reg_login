package ren.lawliet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ren.lawliet.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> { }
