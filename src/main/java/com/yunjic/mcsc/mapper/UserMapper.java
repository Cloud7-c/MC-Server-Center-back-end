package com.yunjic.mcsc.mapper;

import com.yunjic.mcsc.pojo.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author yunyinc
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2023-03-28 23:02:12
* @Entity com.yunjic.mcsc.pojo.model.User
*/
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where user_name = #{userName} limit 1")
    User findUserByUserName(@Param("userName")String userName);
}




