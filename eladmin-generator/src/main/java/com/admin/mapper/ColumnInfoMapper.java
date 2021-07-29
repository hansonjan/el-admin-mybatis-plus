/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.admin.mapper;

import com.admin.domain.ColumnInfo;
import com.admin.domain.TableInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Zheng Jie
 * @date 2019-01-14
 */
@Mapper
@Component
public interface ColumnInfoMapper extends BaseMapper<ColumnInfo> {
    /**
     * 查找某张表的字段列表。
     * @param tableName /
     * @return /
     */
    @Select("<script> select '${tableName}' as tableName,column_name as columnName, case when is_nullable = 'NO' then 1 else 0 end as notNull, data_type as columnType, column_comment as remark, column_key as keyType, " +
            " extra from information_schema.columns  " +
            " where table_name = #{tableName} and table_schema = (select database()) order by ordinal_position" +
            " </script> ")
    List<ColumnInfo> listByTableName(@Param("tableName") String tableName);

    /**
     * 查找当前库所有表。
     * @return /
     */
    @Select("<script> select table_name as tableName,DATE_FORMAT(create_time,'%Y-%m-%d %H:%i:%s') as createTime, engine, table_collation as coding, table_comment as remark" +
            " from information_schema.tables " +
            "  where table_schema = (select database()) " +
            " order by create_time desc" +
            " </script> ")
    List<TableInfo> listTables();

}
