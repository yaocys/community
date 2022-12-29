package com.example.community.actuator;

import com.example.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author yao 2022/12/1
 */
@Component
@Endpoint(id = "myDatabase")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection() {
        try (
                Connection ignored = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "获取数据库连接成功！");
        } catch (SQLException e) {
            logger.error("获取数据库连接失败" + e.getMessage());
            return CommunityUtil.getJSONString(0, "获取数据库连接失败！");
        }
    }
}
