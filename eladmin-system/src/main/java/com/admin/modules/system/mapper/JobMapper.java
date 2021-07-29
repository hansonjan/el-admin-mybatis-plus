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
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-29
*/
@Mapper
@Component
public interface JobMapper extends BaseMapper<Job> {
    /**
     * 根据用户ID查询
     * @param id 用户ID
     * @return /
     */
    @Select("<script>SELECT j.job_id as id,j.name,j.enabled,j.job_sort jobSort,j.create_by as createBy,j.update_by as updateBy,j.create_time as createTime,j.update_time as updateTime" +
            " FROM sys_job j, sys_users_jobs u WHERE " +
            " j.job_id = u.job_id AND u.user_id = #{id}" +
            "</script>")
    Set<Job> findByUserId(Long id);
}
