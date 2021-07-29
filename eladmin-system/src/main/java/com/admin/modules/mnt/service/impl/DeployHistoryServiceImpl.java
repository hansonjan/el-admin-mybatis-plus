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
import com.admin.modules.mnt.domain.DeployHistory;
import com.admin.modules.mnt.mapper.DeployHistoryMapper;
import com.admin.modules.mnt.service.DeployHistoryService;
import com.admin.modules.mnt.service.dto.DeployHistoryDto;
import com.admin.modules.mnt.service.dto.DeployHistoryQueryCriteria;
import com.admin.modules.mnt.service.mapstruct.MapStructDeployHistoryMapper;
import com.admin.utils.FileUtil;
import com.admin.utils.PageUtil;
import com.admin.utils.QueryHelp;
import com.admin.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
public class DeployHistoryServiceImpl extends ServiceImpl<DeployHistoryMapper,DeployHistory> implements DeployHistoryService {

    private final MapStructDeployHistoryMapper mapStructDeployhistoryMapper;

    @Override
    public Object queryAll(DeployHistoryQueryCriteria criteria, IPage pageable){
        QueryWrapper<DeployHistory> queryWrapper = (QueryWrapper<DeployHistory>) QueryHelp.getQueryWrapper(criteria, DeployHistory.class);
        IPage<DeployHistory> page = this.page(pageable, queryWrapper);
        List<DeployHistoryDto> collect = page.getRecords().stream().map(mapStructDeployhistoryMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<DeployHistoryDto> queryAll(DeployHistoryQueryCriteria criteria){
        QueryWrapper<DeployHistory> queryWrapper = (QueryWrapper<DeployHistory>) QueryHelp.getQueryWrapper(criteria, DeployHistory.class);
        List<DeployHistory> list = this.list(queryWrapper);
        return list.stream().map(mapStructDeployhistoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DeployHistoryDto findById(String id) {
        DeployHistory deployhistory = this.getById(id);
        if(ObjectUtil.isNull(deployhistory)){
            deployhistory = new DeployHistory();
        }
        ValidationUtil.isNull(deployhistory.getId(),"DeployHistory","id",id);
        return mapStructDeployhistoryMapper.toDto(deployhistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DeployHistory resources) {
        resources.setId(IdUtil.simpleUUID());
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            this.removeById(id);
        }
    }

    @Override
    public void download(List<DeployHistoryDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeployHistoryDto deployHistoryDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部署编号", deployHistoryDto.getDeployId());
            map.put("应用名称", deployHistoryDto.getAppName());
            map.put("部署IP", deployHistoryDto.getIp());
            map.put("部署时间", deployHistoryDto.getDeployDate());
            map.put("部署人员", deployHistoryDto.getDeployUser());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
