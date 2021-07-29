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
package com.admin.modules.quartz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.exception.BadRequestException;
import com.admin.modules.quartz.domain.QuartzJob;
import com.admin.modules.quartz.domain.QuartzLog;
import com.admin.modules.quartz.repository.QuartzJobMapper;
import com.admin.modules.quartz.repository.QuartzLogMapper;
import com.admin.modules.quartz.service.QuartzJobService;
import com.admin.modules.quartz.service.dto.JobQueryCriteria;
import com.admin.modules.quartz.utils.QuartzManage;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Zheng Jie
 * @date 2019-01-07
 */
@RequiredArgsConstructor
@Service(value = "quartzJobService")
public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper,QuartzJob> implements QuartzJobService {

    private final QuartzLogMapper quartzLogMapper;
    private final QuartzManage quartzManage;
    private final RedisUtils redisUtils;

    @Override
    public Object queryAll(JobQueryCriteria criteria, IPage pageable){
        QueryWrapper<QuartzJob> queryWrapper = (QueryWrapper<QuartzJob>) QueryHelp.getQueryWrapper(criteria, QuartzJob.class);
        IPage<QuartzJob> page = this.page(pageable, queryWrapper);
        return PageUtil.toPage(page);
    }

    @Override
    public Object queryAllLog(JobQueryCriteria criteria, IPage pageable){
        QueryWrapper<QuartzLog> queryWrapper = (QueryWrapper<QuartzLog>) QueryHelp.getQueryWrapper(criteria, QuartzLog.class);
        IPage<QuartzLog> page = quartzLogMapper.selectPage(pageable, queryWrapper);
        return PageUtil.toPage(page);
    }

    @Override
    public List<QuartzJob> queryAll(JobQueryCriteria criteria) {
        QueryWrapper<QuartzJob> queryWrapper = (QueryWrapper<QuartzJob>) QueryHelp.getQueryWrapper(criteria, QuartzJob.class);
        return this.list(queryWrapper);
    }

    @Override
    public List<QuartzLog> queryAllLog(JobQueryCriteria criteria) {
        QueryWrapper<QuartzLog> queryWrapper = (QueryWrapper<QuartzLog>) QueryHelp.getQueryWrapper(criteria, QuartzLog.class);
        return quartzLogMapper.selectList(queryWrapper);
    }

    @Override
    public QuartzJob findById(Long id) {
        QuartzJob quartzJob = baseMapper.selectById(id);
        if(ObjectUtil.isNull(quartzJob)){
            quartzJob = new QuartzJob();
        }
        ValidationUtil.isNull(quartzJob.getId(),"QuartzJob","id",id);
        return quartzJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(QuartzJob resources) {
        if (!CronExpression.isValidExpression(resources.getCronExpression())){
            throw new BadRequestException("cron表达式格式错误");
        }
        this.saveOrUpdate(resources);
        quartzManage.addJob(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuartzJob resources) {
        if (!CronExpression.isValidExpression(resources.getCronExpression())){
            throw new BadRequestException("cron表达式格式错误");
        }
        if(StringUtils.isNotBlank(resources.getSubTask())){
            List<String> tasks = Arrays.asList(resources.getSubTask().split("[,，]"));
            if (tasks.contains(resources.getId().toString())) {
                throw new BadRequestException("子任务中不能添加当前任务ID");
            }
        }
        this.saveOrUpdate(resources);
        quartzManage.updateJobCron(resources);
    }

    @Override
    public void updateIsPause(QuartzJob quartzJob) {
        if (quartzJob.getIsPause()) {
            quartzManage.resumeJob(quartzJob);
            quartzJob.setIsPause(false);
        } else {
            quartzManage.pauseJob(quartzJob);
            quartzJob.setIsPause(true);
        }
        this.saveOrUpdate(quartzJob);
    }

    @Override
    public void execution(QuartzJob quartzJob) {
        quartzManage.runJobNow(quartzJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            QuartzJob quartzJob = findById(id);
            quartzManage.deleteJob(quartzJob);
            this.removeById(quartzJob.getId());
        }
    }

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executionSubJob(String[] tasks) throws InterruptedException {
        for (String id : tasks) {
            QuartzJob quartzJob = findById(Long.parseLong(id));
            // 执行任务
            String uuid = IdUtil.simpleUUID();
            quartzJob.setUuid(uuid);
            // 执行任务
            execution(quartzJob);
            // 获取执行状态，如果执行失败则停止后面的子任务执行
            Boolean result = (Boolean) redisUtils.get(uuid);
            while (result == null) {
                // 休眠5秒，再次获取子任务执行情况
                Thread.sleep(5000);
                result = (Boolean) redisUtils.get(uuid);
            }
            if(!result){
                redisUtils.del(uuid);
                break;
            }
        }
    }

    @Override
    public void download(List<QuartzJob> quartzJobs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuartzJob quartzJob : quartzJobs) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("任务名称", quartzJob.getJobName());
            map.put("Bean名称", quartzJob.getBeanName());
            map.put("执行方法", quartzJob.getMethodName());
            map.put("参数", quartzJob.getParams());
            map.put("表达式", quartzJob.getCronExpression());
            map.put("状态", quartzJob.getIsPause() ? "暂停中" : "运行中");
            map.put("描述", quartzJob.getDescription());
            map.put("创建日期", quartzJob.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void downloadLog(List<QuartzLog> queryAllLog, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuartzLog quartzLog : queryAllLog) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("任务名称", quartzLog.getJobName());
            map.put("Bean名称", quartzLog.getBeanName());
            map.put("执行方法", quartzLog.getMethodName());
            map.put("参数", quartzLog.getParams());
            map.put("表达式", quartzLog.getCronExpression());
            map.put("异常详情", quartzLog.getExceptionDetail());
            map.put("耗时/毫秒", quartzLog.getTime());
            map.put("状态", quartzLog.getIsSuccess() ? "成功" : "失败");
            map.put("创建日期", quartzLog.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<QuartzJob> findByIsPauseIsFalse() {
        LambdaQueryWrapper<QuartzJob> lambdaQuery = Wrappers.lambdaQuery(QuartzJob.class);
        lambdaQuery.eq(QuartzJob::getIsPause,Boolean.FALSE);
        return this.list(lambdaQuery);
    }
}
