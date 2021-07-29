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

import com.admin.domain.GenConfig;
import com.admin.mapper.GenConfigMapper;
import com.admin.service.GenConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Zheng Jie
 * @date 2019-01-14
 */
@Service
@RequiredArgsConstructor
public class GenConfigServiceImpl extends ServiceImpl<GenConfigMapper,GenConfig> implements GenConfigService {

    @Override
    public GenConfig find(String tableName) {
        LambdaQueryWrapper<GenConfig> lambdaQuery = Wrappers.lambdaQuery(GenConfig.class);
        lambdaQuery.eq(GenConfig::getTableName,tableName);
        GenConfig genConfig = this.getOne(lambdaQuery);
        if(genConfig == null){
            return new GenConfig(tableName);
        }
        return genConfig;
    }

    @Override
    public GenConfig update(String tableName, GenConfig genConfig) {
        String separator = File.separator;
        String[] paths;
        String symbol = "\\";
        if (symbol.equals(separator)) {
            paths = genConfig.getPath().split("\\\\");
        } else {
            paths = genConfig.getPath().split(File.separator);
        }
        StringBuilder api = new StringBuilder();
        for (String path : paths) {
            api.append(path);
            api.append(separator);
            if ("src".equals(path)) {
                api.append("api");
                break;
            }
        }
        genConfig.setApiPath(api.toString());
        boolean save = this.saveOrUpdate(genConfig);
        return genConfig;
    }
}
