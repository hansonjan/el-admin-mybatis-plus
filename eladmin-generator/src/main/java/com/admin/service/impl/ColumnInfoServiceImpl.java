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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.domain.ColumnInfo;
import com.admin.mapper.ColumnInfoMapper;
import com.admin.service.ColumnInfoService;
import com.admin.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2019-01-14
 */
@Service
@RequiredArgsConstructor
public class ColumnInfoServiceImpl extends ServiceImpl<ColumnInfoMapper, ColumnInfo> implements ColumnInfoService {

    @Override
    public boolean saveOrUpdateBatch(Collection<ColumnInfo> columnInfos) {
        List<Long> existsIds = Lists.newArrayList();
        if(ObjectUtil.isNotEmpty(columnInfos)){
            Iterator<ColumnInfo> iterator = columnInfos.iterator();
            String tableName = null;
            while(iterator.hasNext()){
                ColumnInfo next = iterator.next();
                if(org.apache.commons.lang3.StringUtils.isBlank(tableName)){
                    tableName = next.getTableName();
                }
                List<ColumnInfo> columns = getColumns(tableName);
                columnInfos.forEach((item)->{
                    columns.forEach(column->{
                        if(item.getTableName().equals(column.getTableName()) && item.getColumnName().equals(column.getColumnName())){
                            item.setId(column.getId());
                            existsIds.add(column.getId());
                        }
                    });
                });
            }
            //删除新的表结构中已经删除的字段。
            LambdaUpdateWrapper<ColumnInfo> lambdaUpdate = Wrappers.lambdaUpdate(ColumnInfo.class);
            lambdaUpdate.eq(ColumnInfo::getTableName,tableName);
            lambdaUpdate.notIn(ColumnInfo::getId,existsIds);
            this.remove(lambdaUpdate);

            return super.saveOrUpdateBatch(columnInfos);
        }
        return false;
    }

    @Override
    public List<ColumnInfo> findByTableNameOrderByIdAsc(String tableName) {
        LambdaQueryWrapper<ColumnInfo> lambdaQuery = Wrappers.lambdaQuery(ColumnInfo.class);
        lambdaQuery.eq(ColumnInfo::getTableName,tableName);
        lambdaQuery.orderByAsc(ColumnInfo::getId);
        List<ColumnInfo> list = this.list(lambdaQuery);
        for (ColumnInfo columnInfo : list) {
            if(ObjectUtil.isNull(columnInfo.getFormShow())){
                columnInfo.setFormShow(Boolean.FALSE);
            }
            if(ObjectUtil.isNull(columnInfo.getListShow())){
                columnInfo.setListShow(Boolean.FALSE);
            }
            if(ObjectUtil.isNull(columnInfo.getNotNull())){
                columnInfo.setNotNull(Boolean.FALSE);
            }
        }
        return list;
    }


    @Override
    public List<ColumnInfo> getColumns(String tableName) {
        List<ColumnInfo> columnInfos = findByTableNameOrderByIdAsc(tableName);
        if (CollectionUtil.isNotEmpty(columnInfos)) {
            return columnInfos;
        } else {
            columnInfos = queryByTableName(tableName);
            //return columnInfoRepository.saveAll(columnInfos);
            this.saveBatch(columnInfos);
            return columnInfos;
        }
    }


    @Override
    public List<ColumnInfo> queryByTableName(String tableName) {
        // 使用预编译防止sql注入
        return baseMapper.listByTableName(tableName);
    }


    @Override
    public void sync(List<ColumnInfo> columnInfos, List<ColumnInfo> columnInfoList) {
        // 第一种情况，数据库类字段改变或者新增字段
        for (ColumnInfo columnInfo : columnInfoList) {
            // 根据字段名称查找
            List<ColumnInfo> columns = columnInfos.stream().filter(c -> c.getColumnName().equals(columnInfo.getColumnName())).collect(Collectors.toList());
            // 如果能找到，就修改部分可能被字段
            if (CollectionUtil.isNotEmpty(columns)) {
                ColumnInfo column = columns.get(0);
                column.setColumnType(columnInfo.getColumnType());
                column.setExtra(columnInfo.getExtra());
                column.setKeyType(columnInfo.getKeyType());
                if (StringUtils.isBlank(column.getRemark())) {
                    column.setRemark(columnInfo.getRemark());
                }
                this.saveOrUpdate(column);
            } else {
                // 如果找不到，则保存新字段信息
                this.saveOrUpdate(columnInfo);
            }
        }
        // 第二种情况，数据库字段删除了
        for (ColumnInfo columnInfo : columnInfos) {
            // 根据字段名称查找
            List<ColumnInfo> columns = columnInfoList.stream().filter(c -> c.getColumnName().equals(columnInfo.getColumnName())).collect(Collectors.toList());
            // 如果找不到，就代表字段被删除了，则需要删除该字段
            if (CollectionUtil.isEmpty(columns)) {
                this.removeById(columnInfo.getId());
            }
        }
    }

    @Override
    public void save(List<ColumnInfo> columnInfos) {
        this.saveBatch(columnInfos);
    }

}
