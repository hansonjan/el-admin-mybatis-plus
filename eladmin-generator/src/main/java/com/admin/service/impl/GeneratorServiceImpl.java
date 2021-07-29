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

import cn.hutool.core.util.ZipUtil;
import com.admin.domain.ColumnInfo;
import com.admin.domain.GenConfig;
import com.admin.domain.TableInfo;
import com.admin.exception.BadRequestException;
import com.admin.mapper.ColumnInfoMapper;
import com.admin.service.GeneratorService;
import com.admin.utils.FileUtil;
import com.admin.utils.GenUtil;
import com.admin.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Zheng Jie
 * @date 2019-01-02
 */
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {
    private static final Logger log = LoggerFactory.getLogger(GeneratorServiceImpl.class);
    private final ColumnInfoMapper columnInfoMapper;
    @Override
    public Object getTables() {
        return columnInfoMapper.listTables();
    }

    @Override
    public Object getTables(String name, int[] startEnd) {
        // 使用预编译防止sql注入
        List<TableInfo> tables = columnInfoMapper.listTables();
        if(startEnd[1] > tables.size()){
            startEnd[1] = tables.size();
        }
        List<TableInfo> subList = tables.subList(startEnd[0], startEnd[1]);

        return PageUtil.toPage(subList, tables.size());
    }



    @Override
    public void generator(GenConfig genConfig, List<ColumnInfo> columns) {
        if (genConfig.getId() == null) {
            throw new BadRequestException("请先配置生成器");
        }
        try {
            GenUtil.generatorCode(columns, genConfig);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BadRequestException("生成失败，请手动处理已生成的文件");
        }
    }

    @Override
    public ResponseEntity<Object> preview(GenConfig genConfig, List<ColumnInfo> columns) {
        if (genConfig.getId() == null) {
            throw new BadRequestException("请先配置生成器");
        }
        List<Map<String, Object>> genList = GenUtil.preview(columns, genConfig);
        return new ResponseEntity<>(genList, HttpStatus.OK);
    }

    @Override
    public void download(GenConfig genConfig, List<ColumnInfo> columns, HttpServletRequest request, HttpServletResponse response) {
        if (genConfig.getId() == null) {
            throw new BadRequestException("请先配置生成器");
        }
        try {
            File file = new File(GenUtil.download(columns, genConfig));
            String zipPath = file.getPath() + ".zip";
            ZipUtil.zip(file.getPath(), zipPath);
            FileUtil.downloadFile(request, response, new File(zipPath), true);
        } catch (IOException e) {
            throw new BadRequestException("打包失败");
        }
    }
}
