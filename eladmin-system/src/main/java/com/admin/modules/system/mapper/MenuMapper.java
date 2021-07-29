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

import com.admin.modules.system.domain.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-12-17
 */
@Mapper
@Component
public interface MenuMapper extends BaseMapper<Menu> {
    /**
     * 根据角色ID与菜单类型查询菜单
     * @param roleIds roleIDs
     * @param type 类型
     * @return /
     */
    @Select("<script> SELECT m.menu_id as id,m.pid,m.sub_count as subCount,m.type ,m.title,m.name as componentName,m.component,m.menu_sort as menuSort,m.icon,m.path,\n" +
            "m.i_frame as iFrame,m.cache,m.hidden,m.permission,\n" +
            "m.create_by as createBy,m.update_by as updateBy,m.create_time as createTime,m.update_time as updateTime" +
            " FROM sys_menu m, sys_roles_menus r WHERE " +
            "m.menu_id = r.menu_id AND r.role_id IN " +
            " <foreach collection=\"roleIds\" index=\"index\" item=\"roleId\" open=\"(\" close=\")\" separator=\",\" >\n" +
            "                    ${roleId}\n" +
            "                </foreach> " +
            " AND type != #{type} order by m.menu_sort asc" +
            " </script>")
    LinkedHashSet<Menu> findByRoleIdsAndTypeNot(@Param("roleIds") Set<Long> roleIds,@Param("type") int type);

}
