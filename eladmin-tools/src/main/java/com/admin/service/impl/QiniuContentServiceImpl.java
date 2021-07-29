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

import com.admin.domain.QiniuConfig;
import com.admin.domain.QiniuContent;
import com.admin.exception.BadRequestException;
import com.admin.mapper.QiniuContentMapper;
import com.admin.service.QiniuContentService;
import com.admin.utils.FileUtil;
import com.admin.utils.QiNiuUtil;
import com.admin.utils.SpringContextHolder;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zheng Jie
 * @date 2018-12-31
 */
@Service
@RequiredArgsConstructor
public class QiniuContentServiceImpl extends ServiceImpl<QiniuContentMapper,QiniuContent> implements QiniuContentService {
    @Value("${qiniu.max-size}")
    private Long maxSize;
    @Override
    public QiniuContent findByKey(String key) {
        LambdaQueryWrapper<QiniuContent> lambdaQuery = Wrappers.lambdaQuery(QiniuContent.class);
        lambdaQuery.eq(QiniuContent::getKey, key);
        return this.getOne(lambdaQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QiniuContent upload(MultipartFile file, QiniuConfig qiniuConfig) {
        FileUtil.checkSize(maxSize, file.getSize());
        if(qiniuConfig.getId() == null){
            throw new BadRequestException("请先添加相应配置，再操作");
        }
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiNiuUtil.getRegion(qiniuConfig.getZone()));
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        String upToken = auth.uploadToken(qiniuConfig.getBucket());
        try {
            String key = file.getOriginalFilename();
            if(findByKey(key) != null) {
                key = QiNiuUtil.getKey(key);
            }
            Response response = uploadManager.put(file.getBytes(), key, upToken);
            //解析上传成功的结果

            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            QiniuContent content = findByKey(FileUtil.getFileNameNoEx(putRet.key));
            if(content == null){
                //存入数据库
                QiniuContent qiniuContent = new QiniuContent();
                qiniuContent.setSuffix(FileUtil.getExtensionName(putRet.key));
                qiniuContent.setBucket(qiniuConfig.getBucket());
                qiniuContent.setType(qiniuConfig.getType());
                qiniuContent.setKey(FileUtil.getFileNameNoEx(putRet.key));
                qiniuContent.setUrl(qiniuConfig.getHost()+"/"+putRet.key);
                qiniuContent.setSize(FileUtil.getSize(Integer.parseInt(file.getSize()+"")));

                this.saveOrUpdate(qiniuContent);
                return qiniuContent;
            }
            return content;
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void synchronize(QiniuConfig config) {
        if(config.getId() == null){
            throw new BadRequestException("请先添加相应配置，再操作");
        }
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiNiuUtil.getRegion(config.getZone()));
        Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        //文件名前缀
        String prefix = "";
        //每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 1000;
        //指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";
        //列举空间文件列表
        BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(config.getBucket(), prefix, limit, delimiter);
        QiniuContentService contentService = SpringContextHolder.getBean(QiniuContentService.class);
        while (fileListIterator.hasNext()) {
            //处理获取的file list结果
            QiniuContent qiniuContent;
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                if(contentService.findByKey(FileUtil.getFileNameNoEx(item.key)) == null){
                    qiniuContent = new QiniuContent();
                    qiniuContent.setSize(FileUtil.getSize(Integer.parseInt(item.fsize+"")));
                    qiniuContent.setSuffix(FileUtil.getExtensionName(item.key));
                    qiniuContent.setKey(FileUtil.getFileNameNoEx(item.key));
                    qiniuContent.setType(config.getType());
                    qiniuContent.setBucket(config.getBucket());
                    qiniuContent.setUrl(config.getHost()+"/"+item.key);
                    contentService.saveOrUpdate(qiniuContent);
                }
            }
        }
    }
}
