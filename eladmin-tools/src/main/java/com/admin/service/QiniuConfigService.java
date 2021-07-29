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
import com.admin.service.dto.QiniuQueryCriteria;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Zheng Jie
 * @date 2018-12-31
 */
public interface QiniuConfigService extends IService<QiniuConfig> {

    /**
     * 查配置
     * @return QiniuConfig
     */
    QiniuConfig find();

    /**
     * 修改配置
     * @param qiniuConfig 配置
     * @return QiniuConfig
     */
    QiniuConfig config(QiniuConfig qiniuConfig);

    /**
     * 分页查询
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Object queryAll(QiniuQueryCriteria criteria, IPage pageable);


    /**
     * 查询文件
     * @param id 文件ID
     * @return QiniuContent
     */
    QiniuContent findByContentId(Long id);

    /**
     * 下载文件
     * @param content 文件信息
     * @param config 配置
     * @return String
     */
    String download(QiniuContent content, QiniuConfig config);

    /**
     * 删除文件
     * @param content 文件
     * @param config 配置
     */
    void delete(QiniuContent content, QiniuConfig config);


    /**
     * 删除文件
     * @param ids 文件ID数组
     * @param config 配置
     */
    void deleteAll(Long[] ids, QiniuConfig config);

    /**
     * 更新数据
     * @param type 类型
     */
    void update(String type);

    /**
     * 导出数据
     * @param queryAll /
     * @param response /
     * @throws IOException /
     */
    void downloadList(List<QiniuContent> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 查询全部
     * @param criteria 条件
     * @return /
     */
    public List<QiniuContent> queryAll(QiniuQueryCriteria criteria);

}
