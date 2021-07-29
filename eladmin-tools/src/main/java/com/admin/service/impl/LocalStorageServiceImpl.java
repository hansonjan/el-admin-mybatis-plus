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
package com.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.admin.config.FileProperties;
import com.admin.domain.LocalStorage;
import com.admin.exception.BadRequestException;
import com.admin.mapper.LocalStorageMapper;
import com.admin.service.LocalStorageService;
import com.admin.service.dto.LocalStorageDto;
import com.admin.service.dto.LocalStorageQueryCriteria;
import com.admin.service.mapstruct.MapStructLocalStorageMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-09-05
*/
@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl extends ServiceImpl<LocalStorageMapper,LocalStorage> implements LocalStorageService {
    private final MapStructLocalStorageMapper mapStructlocalStorageMapper;
    private final FileProperties properties;

    @Override
    public Object queryAll(LocalStorageQueryCriteria criteria, IPage pageable){
        QueryWrapper<LocalStorage> queryWrapper = (QueryWrapper<LocalStorage>) QueryHelp.getQueryWrapper(criteria, LocalStorage.class);
        IPage<LocalStorage> page = this.page(pageable, queryWrapper);
        List<LocalStorageDto> collect = page.getRecords().stream().map(mapStructlocalStorageMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<LocalStorageDto> queryAll(LocalStorageQueryCriteria criteria){
        QueryWrapper<LocalStorage> queryWrapper = (QueryWrapper<LocalStorage>) QueryHelp.getQueryWrapper(criteria, LocalStorage.class);
        List<LocalStorage> list = this.list(queryWrapper);
        return list.stream().map(mapStructlocalStorageMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LocalStorageDto findById(Long id){
        LocalStorage localStorage = baseMapper.selectById(id);
        if(ObjectUtil.isNull(localStorage)){
            localStorage = new LocalStorage();
        }
        ValidationUtil.isNull(localStorage.getId(),"LocalStorage","id",id);
        return mapStructlocalStorageMapper.toDto(localStorage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalStorage create(String name, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type +  File.separator);
        if(ObjectUtil.isNull(file)){
            throw new BadRequestException("上传失败");
        }
        try {
            name = StringUtils.isBlank(name) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : name;
            LocalStorage localStorage = new LocalStorage(
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );
            this.saveOrUpdate(localStorage);
            return localStorage;
        }catch (Exception e){
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LocalStorage resources) {
        LocalStorage localStorage = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNull(localStorage)){
            localStorage = new LocalStorage();
        }
        ValidationUtil.isNull( localStorage.getId(),"LocalStorage","id",resources.getId());
        localStorage.copy(resources);
        this.saveOrUpdate(localStorage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            LocalStorage localStorage = baseMapper.selectById(id);
            if(ObjectUtil.isNull(localStorage)){
                localStorage = new LocalStorage();
            }
            FileUtil.del(localStorage.getPath());
            this.removeById(id);
        }
    }

    @Override
    public void download(List<LocalStorageDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LocalStorageDto localStorageDTO : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("文件名", localStorageDTO.getRealName());
            map.put("备注名", localStorageDTO.getName());
            map.put("文件类型", localStorageDTO.getType());
            map.put("文件大小", localStorageDTO.getSize());
            map.put("创建者", localStorageDTO.getCreateBy());
            map.put("创建日期", localStorageDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
