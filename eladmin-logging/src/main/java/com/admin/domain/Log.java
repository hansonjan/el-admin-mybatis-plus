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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
@Getter
@Setter
@TableName("sys_log")
@NoArgsConstructor
public class Log extends BaseModel implements Serializable {

    @TableId(value="log_id",type= IdType.AUTO)
    private Long id;

    /** 操作用户 */
    @TableField("username")
    private String username;

    /** 描述 */
    @TableField("description")
    private String description;

    /** 方法名 */
    @TableField("method")
    private String method;

    /** 参数 */
    @TableField("params")
    private String params;

    /** 日志类型 */
    @TableField("log_type")
    private String logType;

    /** 请求ip */
    @TableField("request_ip")
    private String requestIp;

    /** 地址 */
    @TableField("address")
    private String address;

    /** 浏览器  */
    @TableField("browser")
    private String browser;

    /** 请求耗时 */
    @TableField("time")
    private Long time;

    /** 异常详细  */
    @TableField("exception_detail")
    private byte[] exceptionDetail;

    /** 创建日期 */
    @TableField("create_time")
    private Timestamp createTime;

    public Log(String logType, Long time) {
        this.logType = logType;
        this.time = time;
    }
}
