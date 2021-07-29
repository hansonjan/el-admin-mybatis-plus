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
import com.admin.domain.QiniuConfig;
import com.admin.domain.QiniuContent;
import com.admin.exception.BadRequestException;
import com.admin.mapper.QiniuConfigMapper;
import com.admin.mapper.QiniuContentMapper;
import com.admin.service.QiniuConfigService;
import com.admin.service.dto.QiniuQueryCriteria;
import com.admin.utils.FileUtil;
import com.admin.utils.QiNiuUtil;
import com.admin.utils.QueryHelp;
import com.admin.utils.ValidationUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zheng Jie
 * @date 2018-12-31
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "qiniuConfig")
public class QiniuConfigServiceImpl extends ServiceImpl<QiniuConfigMapper,QiniuConfig> implements QiniuConfigService {

    private final QiniuContentMapper qiniuContentMapper;

    @Override
    @Cacheable(key = "'config'")
    public QiniuConfig find() {
        QiniuConfig qiniuConfig = baseMapper.selectById(1L);
        if(ObjectUtil.isNull(qiniuConfig)){
            qiniuConfig = new QiniuConfig();
        }
        return qiniuConfig;
    }

    @Override
    @CachePut(key = "'config'")
    @Transactional(rollbackFor = Exception.class)
    public QiniuConfig config(QiniuConfig qiniuConfig) {
        qiniuConfig.setId(1L);
        String http = "http://", https = "https://";
        if (!(qiniuConfig.getHost().toLowerCase().startsWith(http)||qiniuConfig.getHost().toLowerCase().startsWith(https))) {
            throw new BadRequestException("外链域名必须以http://或者https://开头");
        }
        boolean save = this.saveOrUpdate(qiniuConfig);
        return qiniuConfig;
    }

    @Override
    public Object queryAll(QiniuQueryCriteria criteria, IPage pageable){
        QueryWrapper<QiniuConfig> queryWrapper = (QueryWrapper<QiniuConfig>) QueryHelp.getQueryWrapper(criteria, QiniuConfig.class);
        return this.page(pageable,queryWrapper);
    }

    @Override
    public String download(QiniuContent content,QiniuConfig config){
        String finalUrl;
        String type = "公开";
        if(type.equals(content.getType())){
            finalUrl  = content.getUrl();
        } else {
            Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
            // 1小时，可以自定义链接过期时间
            long expireInSeconds = 3600;
            finalUrl = auth.privateDownloadUrl(content.getUrl(), expireInSeconds);
        }
        return finalUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(QiniuContent content, QiniuConfig config) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiNiuUtil.getRegion(config.getZone()));
        Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(content.getBucket(), content.getKey() + "." + content.getSuffix());
            qiniuContentMapper.deleteById(content.getId());
        } catch (QiniuException ex) {
            qiniuContentMapper.deleteById(content.getId());
        }
    }



    @Override
    public void deleteAll(Long[] ids, QiniuConfig config) {
        for (Long id : ids) {
            delete(findByContentId(id), config);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String type) {
        LambdaUpdateWrapper<QiniuConfig> lambdaUpdate = Wrappers.lambdaUpdate(QiniuConfig.class);
        lambdaUpdate.set(QiniuConfig::getType,type);
        this.update(lambdaUpdate);
    }

    @Override
    public void downloadList(List<QiniuContent> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (QiniuContent content : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("文件名", content.getKey());
            map.put("文件类型", content.getSuffix());
            map.put("空间名称", content.getBucket());
            map.put("文件大小", content.getSize());
            map.put("空间类型", content.getType());
            map.put("创建日期", content.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<QiniuContent> queryAll(QiniuQueryCriteria criteria) {
        QueryWrapper<QiniuContent> queryWrapper = (QueryWrapper<QiniuContent>) QueryHelp.getQueryWrapper(criteria, QiniuContent.class);
        return qiniuContentMapper.selectList(queryWrapper);
    }


    @Override
    public QiniuContent findByContentId(Long id) {
        QiniuContent qiniuContent = qiniuContentMapper.selectById(id);
        if(ObjectUtil.isNull(qiniuContent)){
            qiniuContent = new QiniuContent();
        }
        ValidationUtil.isNull(qiniuContent.getId(),"QiniuContent", "id",id);
        return qiniuContent;
    }


}
