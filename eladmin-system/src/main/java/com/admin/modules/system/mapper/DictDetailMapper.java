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

import com.admin.modules.system.domain.DictDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@Mapper
@Component
public interface DictDetailMapper extends BaseMapper<DictDetail> {
    /**
     * 根据字典名称获取字典详情
     * @param name 字典名称
     * @return
     */
    @Select("<script> select d.detail_id as id,d.dict_id as dictId,d.label,d.value,d.dict_sort as dictSort,d.create_by as createBy,\n" +
            "d.update_by as updateBy,d.create_time as createTime,d.update_time as updateTime" +
            " from sys_dict_detail d inner join sys_dict t on d.dict_id = t.dict_id " +
            " where t.name=#{name} </script>")
    public List<DictDetail> findByDictName(String name);
}
