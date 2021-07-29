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
package com.admin.modules.system.domain;

import com.admin.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@Getter
@Setter
@TableName("sys_user")
public class User extends BaseEntity implements Serializable {

    @TableId(value="user_id",type= IdType.AUTO)
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @TableField(exist = false)
    @ApiModelProperty(value = "用户角色")
    private Set<Role> roles;

    @TableField(exist = false)
    @ApiModelProperty(value = "用户岗位")
    private Set<Job> jobs;

    @TableField(exist = false)
    @ApiModelProperty(value = "用户部门")
    private Dept dept;

    @ApiModelProperty(value = "用户部门ID")
    @TableField("dept_id")
    private Long deptId;

    @NotBlank
    @ApiModelProperty(value = "用户名称")
    @TableField("username")
    private String username;

    @NotBlank
    @ApiModelProperty(value = "用户昵称")
    @TableField("nick_name")
    private String nickName;

    @ApiModelProperty(value = "用户性别")
    @TableField("gender")
    private String gender;

    @NotBlank
    @ApiModelProperty(value = "电话号码")
    @TableField("phone")
    private String phone;

    @Email
    @NotBlank
    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "头像真实名称",hidden = true)
    @TableField("avatar_name")
    private String avatarName;

    @ApiModelProperty(value = "头像存储的路径", hidden = true)
    @TableField("avatar_path")
    private String avatarPath;

    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "是否为admin账号", hidden = true)
    @TableField("is_admin")
    private Boolean isAdmin = false;

    @NotNull
    @ApiModelProperty(value = "是否启用")
    @TableField("enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "最后修改密码的时间", hidden = true)
    @TableField("pwd_reset_time")
    private Date pwdResetTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
