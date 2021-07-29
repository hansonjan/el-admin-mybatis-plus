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

import cn.hutool.core.util.ObjectUtil;
import com.admin.modules.mnt.domain.ServerDeploy;
import com.admin.modules.mnt.mapper.ServerDeployMapper;
import com.admin.modules.mnt.service.ServerDeployService;
import com.admin.modules.mnt.service.dto.ServerDeployDto;
import com.admin.modules.mnt.service.dto.ServerDeployQueryCriteria;
import com.admin.modules.mnt.service.mapstruct.MapStructServerDeployMapper;
import com.admin.modules.mnt.util.ExecuteShellUtil;
import com.admin.utils.FileUtil;
import com.admin.utils.PageUtil;
import com.admin.utils.QueryHelp;
import com.admin.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class ServerDeployServiceImpl extends ServiceImpl<ServerDeployMapper,ServerDeploy> implements ServerDeployService {
 
    private final MapStructServerDeployMapper mapStructServerDeployMapper;


    @Override
    public Object queryAll(ServerDeployQueryCriteria criteria, IPage pageable){
        QueryWrapper<ServerDeploy> queryWrapper = (QueryWrapper<ServerDeploy>) QueryHelp.getQueryWrapper(criteria, ServerDeploy.class);
        IPage<ServerDeploy> page = this.page(pageable, queryWrapper);
        List<ServerDeployDto> collect = page.getRecords().stream().map(mapStructServerDeployMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<ServerDeployDto> queryAll(ServerDeployQueryCriteria criteria){
        QueryWrapper<ServerDeploy> queryWrapper = (QueryWrapper<ServerDeploy>) QueryHelp.getQueryWrapper(criteria, ServerDeploy.class);
        List<ServerDeploy> list = this.list(queryWrapper);
        return list.stream().map(mapStructServerDeployMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public ServerDeployDto findById(Long id) {
        ServerDeploy server = this.getById(id);
        if(ObjectUtil.isNull(server)){
            server = new ServerDeploy();
        }
        ValidationUtil.isNull(server.getId(),"ServerDeploy","id",id);
        return mapStructServerDeployMapper.toDto(server);
    }

    @Override
    public ServerDeployDto findByIp(String ip) {
        LambdaQueryWrapper<ServerDeploy> lambdaQuery = Wrappers.lambdaQuery(ServerDeploy.class);
        lambdaQuery.eq(ServerDeploy::getIp,ip);
        ServerDeploy deploy = this.getOne(lambdaQuery);
        return mapStructServerDeployMapper.toDto(deploy);
    }

	@Override
	public Boolean testConnect(ServerDeploy resources) {
		ExecuteShellUtil executeShellUtil = null;
		try {
			executeShellUtil = new ExecuteShellUtil(resources.getIp(), resources.getAccount(), resources.getPassword(),resources.getPort());
			return executeShellUtil.execute("ls")==0;
		} catch (Exception e) {
			return false;
		}finally {
			if (executeShellUtil != null) {
				executeShellUtil.close();
			}
		}
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ServerDeploy resources) {
		this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ServerDeploy resources) {
        ServerDeploy server = this.getById(resources.getId());
        if(ObjectUtil.isNull(server)){
            server = new ServerDeploy();
        }
        ValidationUtil.isNull( server.getId(),"ServerDeploy","id",resources.getId());
        server.copy(resources);
        this.saveOrUpdate(server);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            this.removeById(id);
        }
    }

    @Override
    public void download(List<ServerDeployDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ServerDeployDto deployDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("服务器名称", deployDto.getName());
            map.put("服务器IP", deployDto.getIp());
            map.put("端口", deployDto.getPort());
            map.put("账号", deployDto.getAccount());
            map.put("创建日期", deployDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
