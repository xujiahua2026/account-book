package com.jzbook.accountbook.mapper;

import com.jzbook.accountbook.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM t_user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO t_user(username, password, nickname) VALUES(#{username}, #{password}, #{nickname})")
    int insert(User user);
}