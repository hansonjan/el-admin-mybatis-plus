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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 代码生成配置
 * @author Zheng Jie
 * @date 2019-01-03
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("code_gen_config")
public class GenConfig implements Serializable {

    public GenConfig(String tableName) {
        this.tableName = tableName;
    }

    @TableId(value="config_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "表名")
    @TableField("table_name")
    private String tableName;

    @ApiModelProperty(value = "接口名称")
    @TableField("api_alias")
    private String apiAlias;

    @NotBlank
    @ApiModelProperty(value = "包路径")
    @TableField("pack")
    private String pack;

    @NotBlank
    @ApiModelProperty(value = "模块名")
    @TableField("module_name")
    private String moduleName;

    @NotBlank
    @ApiModelProperty(value = "前端文件路径")
    @TableField("path")
    private String path;

    @ApiModelProperty(value = "前端文件路径")
    @TableField("api_path")
    private String apiPath;

    @ApiModelProperty(value = "作者")
    @TableField("author")
    private String author;

    @ApiModelProperty(value = "表前缀")
    @TableField("prefix")
    private String prefix;

    @ApiModelProperty(value = "是否覆盖")
    @TableField("cover")
    private Boolean cover = false;
}