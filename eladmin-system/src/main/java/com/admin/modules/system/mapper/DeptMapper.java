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
package com.admin.modules.system.mapper;

import com.admin.modules.system.domain.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Mapper
@Component
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 根据角色ID 查询
     * @param roleId 角色ID
     * @return /
     */
    @Select("<script> select d.dept_id as id,d.pid,d.sub_count as subCount,d.name,d.dept_sort as deptSort,d.enabled,d.create_by as createBy,d.update_by as updateBy,d.create_time as createTime,\n" +
            "d.update_time as updateTime " +
            " from sys_dept d, sys_roles_depts u where " +
            " d.dept_id = u.dept_id and u.role_id = #{roleId}" +
            " </script> ")
    Set<Dept> findByRoleId(Long roleId);

}
