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

import cn.hutool.core.util.ObjectUtil;
import com.admin.modules.system.domain.Dict;
import com.admin.modules.system.domain.DictDetail;
import com.admin.modules.system.mapper.DictDetailMapper;
import com.admin.modules.system.mapper.DictMapper;
import com.admin.modules.system.service.DictDetailService;
import com.admin.modules.system.service.dto.DictDetailDto;
import com.admin.modules.system.service.dto.DictDetailQueryCriteria;
import com.admin.modules.system.service.dto.DictSmallDto;
import com.admin.modules.system.service.mapstruct.MapStructDictDetailMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDetailServiceImpl extends ServiceImpl<DictDetailMapper,DictDetail> implements DictDetailService {

    private final DictMapper dictMapper;
    private final MapStructDictDetailMapper mapStructDictDetailMapper;
    private final RedisUtils redisUtils;

    
    @Override
    public Map<String,Object> queryAll(DictDetailQueryCriteria criteria, IPage pageable) {
        QueryWrapper<Dict> query = Wrappers.query();
        List<Map<String, Object>> list = dictMapper.selectMaps(query);

        QueryWrapper<DictDetail> queryWrapper = (QueryWrapper<DictDetail>) QueryHelp.getQueryWrapper(criteria, DictDetail.class);
        IPage<DictDetail> page = this.page(pageable, queryWrapper);
        List<DictDetailDto> collect = page.getRecords().stream().map(mapStructDictDetailMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictDetail resources) {
        this.saveOrUpdate(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetail resources) {
        DictDetail dictDetail = this.getById(resources.getId());
        if(ObjectUtil.isNull(dictDetail)){
            dictDetail = new DictDetail();
        }
        ValidationUtil.isNull( dictDetail.getId(),"DictDetail","id",resources.getId());
        resources.setId(dictDetail.getId());
        this.saveOrUpdate(resources);
        // 清理缓存
        delCaches(resources);
    }


    @Override
    @Cacheable(key = "'name:' + #p0")
    public List<DictDetailDto> getDictByName(String name) {
        List<DictDetail> list = baseMapper.findByDictName(name);
        return list.stream().map(item->{
            DictDetailDto dictDetailDto = mapStructDictDetailMapper.toDto(item);
            DictSmallDto dict = new DictSmallDto();
            dict.setId(item.getDictId());
            dictDetailDto.setDict(dict);
            return dictDetailDto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DictDetail dictDetail = this.getById(id);
        if(ObjectUtil.isNull(dictDetail)){
            dictDetail = new DictDetail();
        }
        // 清理缓存
        delCaches(dictDetail);
        this.removeById(id);
    }

    public void delCaches(DictDetail dictDetail){
        Dict dict = dictMapper.selectById(dictDetail.getDictId());
        if(ObjectUtil.isNull(dict)){
            dict = new Dict();
        }
        redisUtils.del(CacheKey.DICT_NAME + dict.getName());
    }
}
