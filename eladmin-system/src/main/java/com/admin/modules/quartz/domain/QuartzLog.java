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
package com.admin.modules.quartz.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.admin.base.BaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Zheng Jie
 * @date 2019-01-07
 */
@Data
@TableName("sys_quartz_log")
public class QuartzLog extends BaseModel implements Serializable {

    @TableId(value="log_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @ApiModelProperty(value = "任务名称", hidden = true)
    @TableField("job_name")
    private String jobName;

    @ApiModelProperty(value = "bean名称", hidden = true)
    @TableField("bean_name")
    private String beanName;

    @ApiModelProperty(value = "方法名称", hidden = true)
    @TableField("method_name")
    private String methodName;

    @ApiModelProperty(value = "参数", hidden = true)
    @TableField("params")
    private String params;

    @ApiModelProperty(value = "cron表达式", hidden = true)
    @TableField("cron_expression")
    private String cronExpression;

    @ApiModelProperty(value = "状态", hidden = true)
    @TableField("is_success")
    private Boolean isSuccess;

    @ApiModelProperty(value = "异常详情", hidden = true)
    @TableField("exception_detail")
    private String exceptionDetail;

    @ApiModelProperty(value = "执行耗时", hidden = true)
    @TableField("time")
    private Long time;

    @ApiModelProperty(value = "创建时间", hidden = true)
    @TableField("create_time")
    private Timestamp createTime;
}
