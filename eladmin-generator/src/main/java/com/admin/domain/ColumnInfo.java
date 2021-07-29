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
package com.admin.domain;

import com.admin.utils.GenUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 列的数据信息
 * @author Zheng Jie
 * @date 2019-01-02
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("code_column_config")
public class ColumnInfo implements Serializable {

    @TableId(value="column_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @ApiModelProperty(value = "表名")
    @TableField("table_name")
    private String tableName;

    @ApiModelProperty(value = "数据库字段名称")
    @TableField("column_name")
    private String columnName;

    @ApiModelProperty(value = "数据库字段类型")
    @TableField("column_type")
    private String columnType;

    @ApiModelProperty(value = "数据库字段键类型")
    @TableField("key_type")
    private String keyType;

    @ApiModelProperty(value = "字段额外的参数")
    @TableField("extra")
    private String extra;

    @ApiModelProperty(value = "数据库字段描述")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "是否必填")
    @TableField("not_null")
    private Boolean notNull;

    @ApiModelProperty(value = "是否在列表显示")
    @TableField("list_show")
    private Boolean listShow;

    @ApiModelProperty(value = "是否表单显示")
    @TableField("form_show")
    private Boolean formShow;

    @ApiModelProperty(value = "表单类型")
    @TableField("form_type")
    private String formType;

    @ApiModelProperty(value = "查询 1:模糊 2：精确")
    @TableField("query_type")
    private String queryType;

    @ApiModelProperty(value = "字典名称")
    @TableField("dict_name")
    private String dictName;

    @ApiModelProperty(value = "日期注解")
    @TableField("date_annotation")
    private String dateAnnotation;

    public ColumnInfo(String tableName, String columnName, Boolean notNull, String columnType, String remark, String keyType, String extra) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.keyType = keyType;
        this.extra = extra;
        this.notNull = notNull;
        if(GenUtil.PK.equalsIgnoreCase(keyType) && GenUtil.EXTRA.equalsIgnoreCase(extra)){
            this.notNull = false;
        }
        this.remark = remark;
        this.listShow = true;
        this.formShow = true;
    }
}
