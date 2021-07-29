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
package com.admin.service;

import com.admin.domain.QiniuConfig;
import com.admin.domain.QiniuContent;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Zheng Jie
 * @date 2018-12-31
 */
public interface QiniuContentService extends IService<QiniuContent> {
    /**
     * 根据key查询
     * @param key 文件名
     * @return QiniuContent
     */
    QiniuContent findByKey(String key);
    /**
     * 上传文件
     * @param file 文件
     * @param qiniuConfig 配置
     * @return QiniuContent
     */
    public QiniuContent upload(MultipartFile file, QiniuConfig qiniuConfig);
    /**
     * 同步数据
     * @param config 配置
     */
    void synchronize(QiniuConfig config);

}
