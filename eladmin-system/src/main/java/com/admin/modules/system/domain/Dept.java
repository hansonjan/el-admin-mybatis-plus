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
import com.alibaba.fastjson.annotation.JSONField;
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
import java.util.Objects;
import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Getter
@Setter
@TableName("sys_dept")
public class Dept extends BaseEntity implements Serializable {

    @TableId(value="dept_id",type= IdType.AUTO)
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @JSONField(serialize = false)
    @ApiModelProperty(value = "角色")
    @TableField(exist = false)
    private Set<Role> roles;

    @ApiModelProperty(value = "排序")
    @TableField("dept_sort")
    private Integer deptSort;

    @NotBlank
    @ApiModelProperty(value = "部门名称")
    @TableField("name")
    private String name;

    @NotNull
    @ApiModelProperty(value = "是否启用")
    @TableField("enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "上级部门")
    @TableField("pid")
    private Long pid;

    @ApiModelProperty(value = "子节点数目", hidden = true)
    @TableField("sub_count")
    private Integer subCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dept dept = (Dept) o;
        return Objects.equals(id, dept.id) &&
                Objects.equals(name, dept.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
