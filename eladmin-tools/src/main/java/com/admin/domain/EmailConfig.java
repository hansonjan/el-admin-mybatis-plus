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

import com.baomidou.mybatisplus.annotation.TableField;
import com.admin.base.BaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 邮件配置类，数据存覆盖式存入数据存
 * @author Zheng Jie
 * @date 2018-12-26
 */
@Data
@TableName("tool_email_config")
public class EmailConfig extends BaseModel implements Serializable {

    @TableId(value="config_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "邮件服务器SMTP地址")
    @TableField("host")
    private String host;

    @NotBlank
    @ApiModelProperty(value = "邮件服务器 SMTP 端口")
    @TableField("port")
    private String port;

    @NotBlank
    @ApiModelProperty(value = "发件者用户名")
    @TableField("user")
    private String user;

    @NotBlank
    @ApiModelProperty(value = "密码")
    @TableField("pass")
    private String pass;

    @NotBlank
    @ApiModelProperty(value = "收件人")
    @TableField("from_user")
    private String fromUser;
}
