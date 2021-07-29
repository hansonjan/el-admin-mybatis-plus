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

import com.admin.modules.system.domain.Job;
import com.admin.modules.system.domain.Role;
import com.admin.modules.system.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@Mapper
@Component
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据角色中的部门查询
     * @param deptId /
     * @return /
     */
    @Select("<script> SELECT u.user_id as id,u.dept_id as deptId,u.username ,u.nick_name as nickName,u.gender ,u.phone ,u.email ,u.avatar_name as avatarName,u.avatar_path as avatarPath,\n" +
            "u.password ,u.is_admin as isAdmin,u.enabled ,u.create_by as createBy,u.update_by as updateBy,u.pwd_reset_time as pwdResetTime,u.create_time as createTime,\n" +
            "u.update_time as updateTime  FROM sys_user u, sys_users_roles r, sys_roles_depts d WHERE " +
            "u.user_id = r.user_id AND r.role_id = d.role_id AND d.dept_id = #{deptId}" +
            " group by u.user_id" +
            " </script> ")
    List<User> findByRoleDeptId(Long deptId);
    /**
     * 根据菜单查询
     * @param id 菜单ID
     * @return /
     */
    @Select("<script> SELECT u.user_id as id,u.dept_id as deptId,u.username ,u.nick_name as nickName,u.gender ,u.phone ,u.email ,u.avatar_name as avatarName,u.avatar_path as avatarPath,\n" +
            "u.password ,u.is_admin as isAdmin,u.enabled ,u.create_by as createBy,u.update_by as updateBy,u.pwd_reset_time as pwdResetTime,u.create_time as createTime,\n" +
            "u.update_time as updateTime  FROM sys_user u, sys_users_roles ur, sys_roles_menus rm WHERE\n" +
            "u.user_id = ur.user_id AND ur.role_id = rm.role_id AND rm.menu_id = #{id}" +
            " group by u.user_id" +
            " </script> ")
    List<User> findByMenuId(Long id);

    /**
     * 根据角色查询用户
     * @param roleId /
     * @return /
     */
    @Select("<script> SELECT u.user_id as id,u.dept_id as deptId,u.username ,u.nick_name as nickName,u.gender ,u.phone ,u.email ,u.avatar_name as avatarName,u.avatar_path as avatarPath,\n" +
            "u.password ,u.is_admin as isAdmin,u.enabled ,u.create_by as createBy,u.update_by as updateBy,u.pwd_reset_time as pwdResetTime,u.create_time as createTime,\n" +
            "u.update_time as updateTime FROM sys_user u, sys_users_roles r WHERE" +
            " u.user_id = r.user_id AND r.role_id = #{roleId}" +
            "</script>")
    List<User> findByRoleId(Long roleId);
    /**
     * 根据岗位查询
     * @param ids /
     * @return /
     */
    @Select("<script> SELECT count(1) FROM sys_user u, sys_users_jobs j WHERE u.user_id = j.user_id AND j.job_id IN in  " +
            " <foreach collection=\"ids\" index=\"index\" item=\"id\" open=\"(\" close=\")\" separator=\",\" >\n" +
            "   ${id}\n" +
            " </foreach> " +
            " </script> ")
    int countByJobs(@Param("ids") Set<Long> ids);

    /**
     * 根据角色查询
     * @param ids /
     * @return /
     */
    @Select("<script> SELECT count(1) FROM sys_user u, sys_users_roles r WHERE " +
            "u.user_id = r.user_id AND r.role_id in " +
            " <foreach collection=\"ids\" index=\"index\" item=\"id\" open=\"(\" close=\")\" separator=\",\" >\n" +
            "   ${id}\n" +
            " </foreach> " +
            "</script>")
    int countByRoles(@Param("ids") Set<Long> ids);
    /**
     * 修改密码
     * @param username 用户名
     * @param pass 密码
     * @param lastPasswordResetTime /
     */
    @Update("<script> update sys_user set password = #{pass} , pwd_reset_time = #{lastPasswordResetTime} where username = #{username}</script>")
    void updatePass(@Param("username") String username,@Param("pass") String pass,@Param("lastPasswordResetTime") Timestamp lastPasswordResetTime);
    /**
     * 删除用户与角色关联的记录
     * @param userId
     */
    @Delete("<script> delete sys_users_roles where user_id = #{userId}</script>")
    void deleteUserRoles(@Param("userId") Long userId);
    /**
     * 添加用户与角色关联的记录
     * @param userId
     * @param roles
     */
    @Insert("<script> insert into sys_users_roles (user_id,role_id) values " +
            " <foreach collection=\"roles\" index=\"index\" item=\"role\" separator=\",\" >\n" +
            "   (#{userId},#{role.roleId})\n" +
            " </foreach> " +
            "</script>")
    void addUserRoles(@Param("userId") Long userId, @Param("roles")Set<Role> roles);
    /**
     * 删除用户与岗位关联的记录
     * @param userId
     */
    @Delete("<script> delete sys_users_jobs where user_id = #{userId}</script>")
    void deleteUserJobs(@Param("userId") Long userId);
    /**
     * 添加用户与岗位关联的记录
     * @param userId
     * @param jobs
     */
    @Insert("<script> insert into sys_users_jobs (user_id,job_id) values " +
            " <foreach collection=\"jobs\" index=\"index\" item=\"job\" separator=\",\" >\n" +
            "   (#{userId},#{job.jobId}) \n" +
            " </foreach> " +
            "</script>")
    void addUserJobs(@Param("userId") Long userId, @Param("jobs")Set<Job> jobs);
    /**
     * 修改邮箱
     * @param username 用户名
     * @param email 邮箱
     */
    @Update("<script> update sys_user set email = #{email} where username = #{username}</script>")
    void updateEmail(@Param("username") String username,@Param("email")  String email);
}
