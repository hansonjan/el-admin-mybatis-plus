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

import com.admin.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Zheng Jie
 * @date 2019-01-07
 */
@Getter
@Setter
@TableName("sys_quartz_job")
public class QuartzJob extends BaseEntity implements Serializable {

    public static final String JOB_KEY = "JOB_KEY";

    @TableId(value="job_id",type= IdType.AUTO)
    @NotNull(groups = {Update.class})
    private Long id;

    @TableField(exist = false)
    @ApiModelProperty(value = "用于子任务唯一标识", hidden = true)
    private String uuid;

    @ApiModelProperty(value = "定时器名称")
    @TableField("job_name")
    private String jobName;

    @NotBlank
    @ApiModelProperty(value = "Bean名称")
    @TableField("bean_name")
    private String beanName;

    @NotBlank
    @ApiModelProperty(value = "方法名称")
    @TableField("method_name")
    private String methodName;

    @ApiModelProperty(value = "参数")
    @TableField("params")
    private String params;

    @NotBlank
    @ApiModelProperty(value = "cron表达式")
    @TableField("cron_expression")
    private String cronExpression;

    @ApiModelProperty(value = "状态，暂时或启动")
    @TableField("is_pause")
    private Boolean isPause = false;

    @ApiModelProperty(value = "负责人")
    @TableField("person_in_charge")
    private String personInCharge;

    @ApiModelProperty(value = "报警邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "子任务")
    @TableField("sub_task")
    private String subTask;

    @ApiModelProperty(value = "失败后暂停")
    @TableField("pause_after_failure")
    private Boolean pauseAfterFailure;

    @NotBlank
    @ApiModelProperty(value = "备注")
    @TableField("description")
    private String description;
}
