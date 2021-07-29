/*
*  Copyright 2019-2020 ${author}
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
package ${package}.service.impl;

import ${package}.domain.${className};
<#if columns??>
    <#list columns as column>
        <#if column.columnKey = 'UNI'>
            <#if column_index = 1>
import com.admin.exception.EntityExistException;
            </#if>
        </#if>
    </#list>
</#if>
import com.admin.utils.ValidationUtil;
import com.admin.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import ${package}.mapper.${className}Repository;
import ${package}.service.${className}Service;
import ${package}.service.dto.${className}Dto;
import ${package}.service.dto.${className}QueryCriteria;
import ${package}.service.mapstruct.MapStruct${className}Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<#if !auto && pkColumnType = 'Long'>
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
</#if>
<#if !auto && pkColumnType = 'String'>
import cn.hutool.core.util.IdUtil;
</#if>
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.utils.PageUtil;
import com.admin.utils.QueryHelp;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @website https://el-admin.vip
* @description 服务实现
* @author ${author}
* @date ${date}
**/
@Service
@RequiredArgsConstructor
public class ${className}ServiceImpl extends ServiceImpl<${className}Mapper,${className}> implements ${className}Service {

    private final MapStruct${className}Mapper mapStruct${className}Mapper;

    @Override
    public Map<String,Object> queryAll(${className}QueryCriteria criteria, IPage pageable){
        QueryWrapper<${className}> queryWrapper = (QueryWrapper<${className}>) QueryHelp.getQueryWrapper(criteria, ${className}.class);
        IPage<${className}> page = this.page(pageable, queryWrapper);
        List<${className}Dto> collect = page.getRecords().stream().map(mapStruct${className}Mapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<${className}Dto> queryAll(${className}QueryCriteria criteria){
        QueryWrapper<${className}> queryWrapper = (QueryWrapper<${className}>) QueryHelp.getQueryWrapper(criteria, ${className}.class);
        List<${className}> list = this.list(queryWrapper);
        return mapStruct${className}Mapper.toDto(list);
    }

    @Override
    @Transactional
    public ${className}Dto findById(${pkColumnType} ${pkChangeColName}) {
        ${className} ${changeClassName} = baseMapper.selectById(${pkChangeColName});
        if(ObjectUtil.isNull(${changeClassName})){
            ${changeClassName} = new ${className}();
        }
        ValidationUtil.isNull(${changeClassName}.get${pkCapitalColName}(),"${className}","${pkChangeColName}",${pkChangeColName});
        return mapStruct${className}Mapper.toDto(${changeClassName});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ${className}Dto create(${className} resources) {
<#if !auto && pkColumnType = 'Long'>
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        resources.set${pkCapitalColName}(snowflake.nextId()); 
</#if>
<#if !auto && pkColumnType = 'String'>
        resources.set${pkCapitalColName}(IdUtil.simpleUUID()); 
</#if>
<#if columns??>
    <#list columns as column>
    <#if column.columnKey = 'UNI'>
        ${className} ${changeClassName} = this.getById(resources.getId());
        if(ObjectUtil.isNotNull(${changeClassName})){
            throw new EntityExistException(${className}.class,"${column.columnName}",resources.get${column.capitalColumnName}());
        }
        if(${changeClassName}Repository.findBy${column.capitalColumnName}(resources.get${column.capitalColumnName}()) != null){
            throw new EntityExistException(${className}.class,"${column.columnName}",resources.get${column.capitalColumnName}());
        }
    </#if>
    </#list>
</#if>
        this.saveOrUpdate(resources);
        return mapStruct${className}Mapper.toDto(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(${className} resources) {
        ${className} ${changeClassName} = this.getById(resources.get${pkCapitalColName}());
        if(ObjectUtil.isNull(${changeClassName})){
            ${changeClassName} = new ${className}();
        }
        ValidationUtil.isNull( ${changeClassName}.get${pkCapitalColName}(),"${className}","id",resources.get${pkCapitalColName}());
<#if columns??>
    <#list columns as column>
        <#if column.columnKey = 'UNI'>
        <#if column_index = 1>
        ${className} ${changeClassName}1 = null;
        </#if>
        LambdaQueryWrapper<${className}> queryWrapper = Wrappers.lambdaQuery(${className}.class);
        queryWrapper.eq(${className}::get${column.capitalColumnName},resources.get${column.capitalColumnName}());

        ${changeClassName}1 = this.getOne(queryWrapper);
        if(ObjectUtil.isNotNull(${changeClassName}1) && !${changeClassName}1.get${pkCapitalColName}().equals(${changeClassName}.get${pkCapitalColName}())){
            throw new EntityExistException(${className}.class,"${column.columnName}",resources.get${column.capitalColumnName}());
        }
        </#if>
    </#list>
</#if>
        ${changeClassName}.copy(resources);
        this.saveOrUpdate(resources);
    }

    @Override
    public void deleteAll(${pkColumnType}[] ids) {
        List<${pkColumnType}> idList = Arrays.asList(ids);
        this.removeByIds(idList);
    }

    @Override
    public void download(List<${className}Dto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (${className}Dto ${changeClassName} : all) {
            Map<String,Object> map = new LinkedHashMap<>();
        <#list columns as column>
            <#if column.columnKey != 'PRI'>
            <#if column.remark != ''>
            map.put("${column.remark}", ${changeClassName}.get${column.capitalColumnName}());
            <#else>
            map.put(" ${column.changeColumnName}",  ${changeClassName}.get${column.capitalColumnName}());
            </#if>
            </#if>
        </#list>
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
