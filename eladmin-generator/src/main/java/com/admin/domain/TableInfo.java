package com.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author: hansonjan
 * @Date: 2021/7/18 0:16
 */
@Data
public class TableInfo {
    @TableId(value="table_name",type= IdType.INPUT)
    private String tableName;
    private String engine;
    private String coding;
    private String remark;
    private String createTime;
}
