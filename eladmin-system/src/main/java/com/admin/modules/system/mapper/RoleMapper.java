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

import com.admin.modules.system.domain.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-12-03
 */
@Mapper
@Component
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 解绑角色菜单
     * @param id 菜单ID
     */
    @Update("delete from sys_roles_menus where menu_id = #{id}")
    void untiedMenu(Long id);


    /**
     * 根据角色ID 查询
     * @param deptId 角色ID
     * @return /
     */
    @Select("<script> select r.role_id as id,r.name,r.level,r.description,r.data_scope as dataScope,r.create_by as createBy,r.update_by as updateBy,r.create_time as createTime,\n" +
            "r.update_time as updateTime " +
            " from sys_role r, sys_roles_depts u where " +
            " r.role_id = u.role_id and u.dept_id = #{deptId}" +
            " </script> ")
    Set<Role> findByDeptId(Long deptId);

    /**
     * 根据用户ID查询
     * @param id 用户ID
     * @return /
     */
    @Select("<script>SELECT r.role_id as id,r.name,r.level,r.description,r.data_scope as dataScope,r.create_by as createBy,r.update_by as updateBy,r.create_time as createTime,\n" +
            "r.update_time as updateTime " +
            " FROM sys_role r, sys_users_roles u WHERE " +
            "r.role_id = u.role_id AND u.user_id = #{id}" +
            "</script>")
    Set<Role> findByUserId(Long id);
    /**
     * 根据菜单Id查询
     * @param menuIds /
     * @return /
     */
    @Select("<script>SELECT r.role_id as id,r.name,r.level,r.description,r.data_scope as dataScope,r.create_by as createBy,r.update_by as updateBy,r.create_time as createTime,\n" +
            "r.update_time as updateTime " +
            " FROM sys_role r, sys_roles_menus m WHERE " +
            " r.role_id = m.role_id AND m.menu_id in " +
            " <foreach collection=\"menuIds\" index=\"index\" item=\"id\" open=\"(\" close=\")\" separator=\",\" >\n" +
            "   ${id}\n" +
            " </foreach> " +
            "</script>")
    List<Role> findInMenuId(@Param("menuIds") List<Long> menuIds);
}
