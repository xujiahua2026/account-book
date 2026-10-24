package com.jzbook.accountbook.mapper;

import com.jzbook.accountbook.entity.Record;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecordMapper {

    @Insert("INSERT INTO t_record(user_id, type, category, amount, record_date, remark) " +
            "VALUES(#{userId}, #{type}, #{category}, #{amount}, #{recordDate}, #{remark})")
    int insert(Record record);

    @Select("SELECT * FROM t_record WHERE user_id = #{userId} ORDER BY record_date DESC, id DESC")
    List<Record> listByUserId(Long userId);

    @Delete("DELETE FROM t_record WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);


    @Select("SELECT " +
            "IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS expense, " +
            "IFNULL(SUM(CASE WHEN type = 2 THEN amount ELSE 0 END), 0) AS income " +
            "FROM t_record WHERE user_id = #{userId} " +
            "AND DATE_FORMAT(record_date, '%Y-%m') = #{month}")
    java.util.Map<String, Object> sumByMonth(@Param("userId") Long userId, @Param("month") String month);

}
