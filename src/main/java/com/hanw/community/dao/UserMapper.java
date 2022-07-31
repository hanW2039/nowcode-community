package com.hanw.community.dao;

import com.hanw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hanW
 * @create 2022-07-30 13:52
 */
//mapper == dao,这个接口不需要实现
@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    //返回成功添加几人
    int insertUser(User user);

    int updateStatus(int id,int status);

    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);
}
