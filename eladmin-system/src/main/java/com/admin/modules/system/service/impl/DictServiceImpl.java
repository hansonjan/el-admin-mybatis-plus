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
package com.admin.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.modules.system.domain.Dict;
import com.admin.modules.system.mapper.DictMapper;
import com.admin.modules.system.service.DictService;
import com.admin.modules.system.service.dto.DictDetailDto;
import com.admin.modules.system.service.dto.DictDto;
import com.admin.modules.system.service.dto.DictQueryCriteria;
import com.admin.modules.system.service.mapstruct.MapStructDictMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictServiceImpl extends ServiceImpl<DictMapper,Dict> implements DictService {
    private final MapStructDictMapper mapStructDictMapper;
    private final RedisUtils redisUtils;

    @Override
    public Map<String, Object> queryAll(DictQueryCriteria criteria, IPage pageable){
        QueryWrapper<Dict> queryWrapper = (QueryWrapper<Dict>) QueryHelp.getQueryWrapper(criteria, Dict.class);
        IPage<Dict> page = this.page(pageable, queryWrapper);
        List<DictDto> collect = page.getRecords().stream().map(mapStructDictMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<DictDto> queryAll(DictQueryCriteria criteria) {
        QueryWrapper<Dict> queryWrapper = (QueryWrapper<Dict>) QueryHelp.getQueryWrapper(criteria, Dict.class);
        List<Dict> list = this.list(queryWrapper);
        return mapStructDictMapper.toDto(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dict resources) {
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict resources) {
        // 清理缓存
        delCaches(resources);
        Dict dict = this.getById(resources.getId());
        if(ObjectUtil.isNull(dict)){
            dict = new Dict();
        }
        ValidationUtil.isNull( dict.getId(),"Dict","id",resources.getId());
        dict.setName(resources.getName());
        dict.setDescription(resources.getDescription());
        this.saveOrUpdate(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        LambdaQueryWrapper<Dict> lambdaQuery = Wrappers.lambdaQuery(Dict.class);
        lambdaQuery.in(Dict::getId,ids);
        List<Dict> dicts = this.list(lambdaQuery);
        // 清理缓存
        for (Dict dict : dicts) {
            delCaches(dict);
        }
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public void download(List<DictDto> dictDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictDto dictDTO : dictDtos) {
            if(CollectionUtil.isNotEmpty(dictDTO.getDictDetails())){
                for (DictDetailDto dictDetail : dictDTO.getDictDetails()) {
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("字典名称", dictDTO.getName());
                    map.put("字典描述", dictDTO.getDescription());
                    map.put("字典标签", dictDetail.getLabel());
                    map.put("字典值", dictDetail.getValue());
                    map.put("创建日期", dictDetail.getCreateTime());
                    list.add(map);
                }
            } else {
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("字典名称", dictDTO.getName());
                map.put("字典描述", dictDTO.getDescription());
                map.put("字典标签", null);
                map.put("字典值", null);
                map.put("创建日期", dictDTO.getCreateTime());
                list.add(map);
            }
        }
        FileUtil.downloadExcel(list, response);
    }

    public void delCaches(Dict dict){
        redisUtils.del(CacheKey.DICT_NAME + dict.getName());
    }
}
