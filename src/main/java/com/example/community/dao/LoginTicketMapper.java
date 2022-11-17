package com.example.community.dao;

import com.example.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author yao 2022/4/28
 */
@Mapper
public interface LoginTicketMapper {
    /**
     * 登陆成功插入一个凭证
     * @param loginTicket 登录凭证对象
     */
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired" ,
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    /**
     * 修改登陆状态，而不是真的删除
     * @param ticket 凭证字段
     * @param status 状态
     */
    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(String ticket,int status);

}
