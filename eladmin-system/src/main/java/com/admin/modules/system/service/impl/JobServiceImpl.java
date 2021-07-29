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
import com.admin.exception.BadRequestException;
import com.admin.exception.EntityExistException;
import com.admin.modules.system.domain.Job;
import com.admin.modules.system.mapper.JobMapper;
import com.admin.modules.system.mapper.UserMapper;
import com.admin.modules.system.service.JobService;
import com.admin.modules.system.service.dto.JobDto;
import com.admin.modules.system.service.dto.JobQueryCriteria;
import com.admin.modules.system.service.mapstruct.MapStructJobMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-03-29
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "job")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    private final MapStructJobMapper mapStructJobMapper;
    private final RedisUtils redisUtils;
    private final UserMapper userMapper;

    @Override
    public Map<String,Object> queryAll(JobQueryCriteria criteria, IPage pageable) {
        QueryWrapper<Job> queryWrapper = (QueryWrapper<Job>) QueryHelp.getQueryWrapper(criteria, Job.class);
        IPage<Job> page = this.page(pageable, queryWrapper);
        List<JobDto> collect = page.getRecords().stream().map(mapStructJobMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<JobDto> queryAll(JobQueryCriteria criteria) {
        QueryWrapper<Job> queryWrapper = (QueryWrapper<Job>) QueryHelp.getQueryWrapper(criteria, Job.class);
        List<Job> list = this.list(queryWrapper);
        return mapStructJobMapper.toDto(list);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public JobDto findById(Long id) {
        Job job = baseMapper.selectById(id);
        if(ObjectUtil.isNull(job)){
            job = new Job();
        }
        ValidationUtil.isNull(job.getId(),"Job","id",id);
        return mapStructJobMapper.toDto(job);
    }

    @Override
    public JobDto findByName(String name) {
        LambdaQueryWrapper<Job> lambdaQuery = Wrappers.lambdaQuery(Job.class);
        lambdaQuery.eq(Job::getName,name);
        return mapStructJobMapper.toDto(this.getOne(lambdaQuery));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Job resources) {
        Job job = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNotNull(job)){
            throw new EntityExistException(Job.class,"name",resources.getName());
        }
        this.saveOrUpdate(resources);
    }

    @Override
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(Job resources) {
        Job job = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNull(job)){
            job = new Job();
        }
        JobDto old = findByName(resources.getName());
        if(old != null && !old.getId().equals(resources.getId())){
            throw new EntityExistException(Job.class,"name",resources.getName());
        }
        ValidationUtil.isNull( job.getId(),"Job","id",resources.getId());
        resources.setId(job.getId());
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        baseMapper.deleteBatchIds(ids);
        // 删除缓存
        redisUtils.delByKeys(CacheKey.JOB_ID, ids);
    }

    @Override
    public void download(List<JobDto> jobDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobDto jobDTO : jobDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("岗位名称", jobDTO.getName());
            map.put("岗位状态", jobDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", jobDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if(userMapper.countByJobs(ids) > 0){
            throw new BadRequestException("所选的岗位中存在用户关联，请解除关联再试！");
        }
    }
}
