package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 网站数据统计
 * @author yao 2022/12/1
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * 将指定IP计入UV
     */
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    /**
     * 将指定用户计入DAU
     */
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    /**
     * 统计指定日期范围内的UV
     */
    public long calculateUV(Date start,Date end){
        /*
         * 整理该日期范围内的key
         */
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        // 如果时间不晚于结束之间就累计
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(key);
            // 更新日期，加一天
            calendar.add(Calendar.DATE, 1);
        }
        /*
        合并数据
         */
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(start), dateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * 统计指定日期范围内的DAU
     */
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        /*
        整理该日期范围内的key
         */
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        /*
        进行OR运算
         */
        return (long) redisTemplate.execute((RedisCallback) connection -> {
            String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(start), dateFormat.format(end));
            connection.bitOp(RedisStringCommands.BitOperation.OR,
                    redisKey.getBytes(), keyList.toArray(new byte[0][0]));
            return connection.bitCount(redisKey.getBytes());
        });
    }
}
