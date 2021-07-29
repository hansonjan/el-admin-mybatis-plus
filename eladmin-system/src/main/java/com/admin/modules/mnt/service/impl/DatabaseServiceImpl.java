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
package com.admin.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.modules.mnt.domain.Database;
import com.admin.modules.mnt.mapper.DatabaseMapper;
import com.admin.modules.mnt.service.DatabaseService;
import com.admin.modules.mnt.service.dto.DatabaseDto;
import com.admin.modules.mnt.service.dto.DatabaseQueryCriteria;
import com.admin.modules.mnt.service.mapstruct.MapStructDatabaseMapper;
import com.admin.modules.mnt.util.SqlUtils;
import com.admin.utils.FileUtil;
import com.admin.utils.PageUtil;
import com.admin.utils.QueryHelp;
import com.admin.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author zhanghouying
* @date 2019-08-24
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseServiceImpl extends ServiceImpl<DatabaseMapper,Database> implements DatabaseService {

    private final MapStructDatabaseMapper mapStructDatabaseMapper;

    @Override
    public Object queryAll(DatabaseQueryCriteria criteria, IPage pageable){
        QueryWrapper<Database> queryWrapper = (QueryWrapper<Database>) QueryHelp.getQueryWrapper(criteria, Database.class);
        IPage<Database> page = this.page(pageable, queryWrapper);
        List<DatabaseDto> collect = page.getRecords().stream().map(mapStructDatabaseMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<DatabaseDto> queryAll(DatabaseQueryCriteria criteria){
        QueryWrapper<Database> queryWrapper = (QueryWrapper<Database>) QueryHelp.getQueryWrapper(criteria, Database.class);
        List<Database> list = this.list(queryWrapper);
        return list.stream().map(mapStructDatabaseMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DatabaseDto findById(String id) {
        Database database = this.getById(id);
        if(ObjectUtil.isNull(database)){
            database = new Database();
        }
        ValidationUtil.isNull(database.getId(),"Database","id",id);
        return mapStructDatabaseMapper.toDto(database);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Database resources) {
        resources.setId(IdUtil.simpleUUID());
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Database resources) {
        Database database = this.getById(resources.getId());
        if(ObjectUtil.isNull(database)){
            database = new Database();
        }
        ValidationUtil.isNull(database.getId(),"Database","id",resources.getId());
        database.copy(resources);
        this.saveOrUpdate(database);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            this.removeById(id);
        }
    }

	@Override
	public boolean testConnection(Database resources) {
		try {
			return SqlUtils.testConnection(resources.getJdbcUrl(), resources.getUserName(), resources.getPwd());
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
	}

    @Override
    public void download(List<DatabaseDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DatabaseDto databaseDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("数据库名称", databaseDto.getName());
            map.put("数据库连接地址", databaseDto.getJdbcUrl());
            map.put("用户名", databaseDto.getUserName());
            map.put("创建日期", databaseDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
