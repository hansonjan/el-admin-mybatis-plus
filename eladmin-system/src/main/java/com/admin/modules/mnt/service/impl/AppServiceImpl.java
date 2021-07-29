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
import com.admin.exception.BadRequestException;
import com.admin.modules.mnt.domain.App;
import com.admin.modules.mnt.mapper.AppMapper;
import com.admin.modules.mnt.service.AppService;
import com.admin.modules.mnt.service.dto.AppDto;
import com.admin.modules.mnt.service.dto.AppQueryCriteria;
import com.admin.modules.mnt.service.mapstruct.MapStructAppMapper;
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
public class AppServiceImpl extends ServiceImpl<AppMapper,App> implements AppService {

    private final MapStructAppMapper mapStructAppMapper;

    @Override
    public Object queryAll(AppQueryCriteria criteria, IPage pageable){
        QueryWrapper<App> queryWrapper = (QueryWrapper<App>) QueryHelp.getQueryWrapper(criteria, App.class);
        IPage<App> page = this.page(pageable, queryWrapper);
        List<AppDto> collect = page.getRecords().stream().map(mapStructAppMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<AppDto> queryAll(AppQueryCriteria criteria){
        QueryWrapper<App> queryWrapper = (QueryWrapper<App>) QueryHelp.getQueryWrapper(criteria, App.class);
        List<App> list = this.list(queryWrapper);
        return list.stream().map(mapStructAppMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AppDto findById(Long id) {
        App app = this.getById(id);
        if(ObjectUtil.isNull(app)){
            app = new App();
        }
        ValidationUtil.isNull(app.getId(),"App","id",id);
        return mapStructAppMapper.toDto(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(App resources) {
        verification(resources);
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(App resources) {
        verification(resources);
        App app = this.getById(resources.getId());
        if(ObjectUtil.isNull(app)){
            app = new App();
        }
        ValidationUtil.isNull(app.getId(),"App","id",resources.getId());
        app.copy(resources);
        this.saveOrUpdate(app);
    }

    private void verification(App resources){
        String opt = "/opt";
        String home = "/home";
        if (!(resources.getUploadPath().startsWith(opt) || resources.getUploadPath().startsWith(home))) {
            throw new BadRequestException("文件只能上传在opt目录或者home目录 ");
        }
        if (!(resources.getDeployPath().startsWith(opt) || resources.getDeployPath().startsWith(home))) {
            throw new BadRequestException("文件只能部署在opt目录或者home目录 ");
        }
        if (!(resources.getBackupPath().startsWith(opt) || resources.getBackupPath().startsWith(home))) {
            throw new BadRequestException("文件只能备份在opt目录或者home目录 ");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            this.removeById(id);
        }
    }

    @Override
    public void download(List<AppDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AppDto appDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("应用名称", appDto.getName());
            map.put("端口", appDto.getPort());
            map.put("上传目录", appDto.getUploadPath());
            map.put("部署目录", appDto.getDeployPath());
            map.put("备份目录", appDto.getBackupPath());
            map.put("启动脚本", appDto.getStartScript());
            map.put("部署脚本", appDto.getDeployScript());
            map.put("创建日期", appDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
