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
package com.admin.modules.mnt.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.admin.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
* @author zhanghouying
* @date 2019-08-24
*/
@Getter
@Setter
@TableName("mnt_database")
public class Database extends BaseEntity implements Serializable {

    @TableId(value="db_id",type= IdType.INPUT)
    @ApiModelProperty(value = "ID", hidden = true)
    private String id;

	@ApiModelProperty(value = "数据库名称")
    @TableField("name")
    private String name;

	@ApiModelProperty(value = "数据库连接地址")
    @TableField("jdbc_url")
    private String jdbcUrl;

	@ApiModelProperty(value = "数据库密码")
    @TableField("pwd")
    private String pwd;

	@ApiModelProperty(value = "用户名")
    @TableField("user_name")
    private String userName;

    public void copy(Database source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
