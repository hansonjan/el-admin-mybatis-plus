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

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.admin.domain.Log;
import com.admin.mapper.LogMapper;
import com.admin.service.LogService;
import com.admin.service.dto.LogErrorDTO;
import com.admin.service.dto.LogQueryCriteria;
import com.admin.service.dto.LogSmallDTO;
import com.admin.service.mapstruct.MapStructLogErrorMapper;
import com.admin.service.mapstruct.MapStructLogSmallMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<LogMapper,Log> implements LogService {
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);
    private final MapStructLogErrorMapper mapStructLogErrorMapper;
    private final MapStructLogSmallMapper mapStructLogSmallMapper;

    @Override
    public Object queryAll(LogQueryCriteria criteria, IPage pageable) {
        QueryWrapper queryWrapper = QueryHelp.getQueryWrapper(criteria, Log.class);
        IPage<Log> page = this.page(pageable, queryWrapper);
        String status = "ERROR";
        if (status.equals(criteria.getLogType())) {
            List<LogErrorDTO> collect = page.getRecords().stream().map(mapStructLogErrorMapper::toDto).collect(Collectors.toList());
            return PageUtil.toPage(page,collect);
        }
        return page;
    }

    @Override
    public List<Log> queryAll(LogQueryCriteria criteria) {
        QueryWrapper queryWrapper = QueryHelp.getQueryWrapper(criteria, Log.class);
        return this.list(queryWrapper);
    }

    @Override
    public Object queryAllByUser(LogQueryCriteria criteria, IPage pageable) {
        QueryWrapper queryWrapper = QueryHelp.getQueryWrapper(criteria, Log.class);
        IPage<Log> page = this.page(pageable, queryWrapper);
        List<LogSmallDTO> collect = page.getRecords().stream().map(mapStructLogSmallMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.admin.annotation.Log aopLog = method.getAnnotation(com.admin.annotation.Log.class);

        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";

        // 描述
        if (log != null) {
            log.setDescription(aopLog.value());
        }
        assert log != null;
        log.setRequestIp(ip);

        log.setAddress(StringUtils.getCityInfo(log.getRequestIp()));
        log.setMethod(methodName);
        log.setUsername(username);
        log.setParams(getParameter(method, joinPoint.getArgs()));
        log.setBrowser(browser);
        this.saveOrUpdate(log);
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private String getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return "";
        }
        return argList.size() == 1 ? JSONUtil.toJsonStr(argList.get(0)) : JSONUtil.toJsonStr(argList);
    }

    @Override
    public Object findByErrDetail(Long id) {
        Log log = baseMapper.selectById(id);
        ValidationUtil.isNull(log, "Log", "id", id);
        ValidationUtil.isNull(log.getId(), "Log", "id", id);
        byte[] details = log.getExceptionDetail();
        return Dict.create().set("exception", new String(ObjectUtil.isNotNull(details) ? details : "".getBytes()));
    }

    @Override
    public void download(List<Log> logs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : logs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("IP", log.getRequestIp());
            map.put("IP来源", log.getAddress());
            map.put("描述", log.getDescription());
            map.put("浏览器", log.getBrowser());
            map.put("请求耗时/毫秒", log.getTime());
            map.put("异常详情", new String(ObjectUtil.isNotNull(log.getExceptionDetail()) ? log.getExceptionDetail() : "".getBytes()));
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByError() {
        LambdaUpdateWrapper<Log> lambdaUpdate = Wrappers.lambdaUpdate(Log.class);
        lambdaUpdate.eq(Log::getLogType, "ERROR");
        this.update(lambdaUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByInfo() {
        LambdaUpdateWrapper<Log> lambdaUpdate = Wrappers.lambdaUpdate(Log.class);
        lambdaUpdate.eq(Log::getLogType, "INFO");
        this.update(lambdaUpdate);
    }
}
