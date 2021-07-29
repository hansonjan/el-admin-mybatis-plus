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
package com.admin.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.annotation.DataPermission;
import com.admin.annotation.Query;
import com.admin.base.BaseModel;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Zheng Jie
 * @date 2019-6-4 14:59:48
 */
@Slf4j
@SuppressWarnings({"unchecked","all"})
public class QueryHelp {

    public static QueryWrapper<? extends BaseModel> getQueryWrapper(Object query,Class<? extends BaseModel> aclass) {
        QueryWrapper<? extends BaseModel> lambdaQuery = Wrappers.query();
        if(query == null){
            return lambdaQuery;
        }
        // 数据权限验证
        DataPermission permission = query.getClass().getAnnotation(DataPermission.class);
        if(permission != null){
            // 获取数据权限
            //目前只有dept做树状联表查询。
            List<Long> dataScopes = SecurityUtils.getCurrentUserDataScope();
            if(CollectionUtil.isNotEmpty(dataScopes)){
                if(StringUtils.isNotBlank(permission.joinName()) && StringUtils.isNotBlank(permission.fieldName())) {
                    //TODO
//                    Join join = root.join(permission.joinName(), JoinType.LEFT);
//                    list.add(getExpression(permission.fieldName(),join, root).in(dataScopes));

                } else if (StringUtils.isBlank(permission.joinName()) && StringUtils.isNotBlank(permission.fieldName())) {
                    //TODO
//                    list.add(getExpression(permission.fieldName(),null, root).in(dataScopes));
                }
            }
        }
        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                // 设置对象的访问权限，保证对private的属性的访
                field.setAccessible(true);
                Query q = field.getAnnotation(Query.class);
                if (q != null) {
                    String propName = q.propName();
                    String joinName = q.joinName();
                    String joinClassName = q.joinClassName();
                    String joinFieldName = q.joinFieldName();
                    String blurry = q.blurry();
                    String attributeName = isBlank(propName) ? toColumnName(field.getName()) : propName;
                    Class<?> fieldType = field.getType();
                    Object val = field.get(query);
                    if (ObjectUtil.isNull(val) || "".equals(val)) {
                        continue;
                    }
                    // 模糊多字段
                    if (ObjectUtil.isNotEmpty(blurry)) {
                        String[] blurrys = blurry.split(",");

                        QueryWrapper<? extends BaseModel> orLike = lambdaQuery.or();
                        for (String fieldName : blurrys) {
                            String columnName = getColumnName(aclass, fieldName);
                            orLike.like(columnName,val.toString());
                        }
                        continue;
                    }
                    if (ObjectUtil.isNotEmpty(joinName)) {
                        //POJO类字段
                        Field joinSelfField = getField(aclass,joinName);
                        if(ObjectUtil.isNull(joinSelfField)){
                            continue;
                        }
                        Class<?> joinSelfClass = joinSelfField.getType();
                        TableField pojoSelfField = joinSelfField.getAnnotation(TableField.class);
                        if(ObjectUtil.isNotNull(pojoSelfField) && ObjectUtil.isNotNull(joinClassName) && ObjectUtil.isNotNull(joinFieldName)){
                            Class<?> aClass = Class.forName(joinClassName);
                            Field field1 = getField(aClass,joinFieldName);
                            if(ObjectUtil.isNull(field1)){
                                continue;
                            }
                            String joinSelfColumn = pojoSelfField.value();
                            TableField annotation = field1.getAnnotation(TableField.class);
                            String joinColumnName = joinColumnName = toColumnName(joinFieldName);
                            String simpleName = aClass.getSimpleName();
                            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                            String serviceName = simpleName + "ServiceImpl";
                            IService service = null;
                            try {
                                service = SpringContextHolder.getBean(serviceName);
                            }catch(Exception es){
                            }
                            if(ObjectUtil.isNull(service)){
                                continue;
                            }

                            QueryWrapper queryWrapper = Wrappers.query();
                            queryWrapper.eq(propName, val);
                            List<Map> list = service.listMaps(queryWrapper);
                            if(joinSelfClass.equals(Long.class)){
                                List<Long> ids = new ArrayList<>();
                                for (Map map : list) {
                                    ids.add(MapUtils.getLong(map,joinColumnName));
                                }
                                lambdaQuery.in(joinSelfColumn,ids);
                            }
                            if(joinSelfClass.equals(Integer.class)){
                                List<Integer> ids = new ArrayList<>();
                                for (Map map : list) {
                                    ids.add(MapUtils.getInteger(map,joinColumnName));
                                }
                                lambdaQuery.in(joinSelfColumn,ids);
                            }
                            if(joinSelfClass.equals(Double.class)){
                                List<Double> ids = new ArrayList<>();
                                for (Map map : list) {
                                    ids.add(MapUtils.getDouble(map,joinColumnName));
                                }
                                lambdaQuery.in(joinSelfColumn,ids);
                            }
                            if(joinSelfClass.equals(Float.class)){
                                List<Float> ids = new ArrayList<>();
                                for (Map map : list) {
                                    ids.add(MapUtils.getFloat(map,joinColumnName));
                                }
                                lambdaQuery.in(joinSelfColumn,ids);
                            }
                            if(joinSelfClass.equals(String.class)){
                                List<String> ids = new ArrayList<>();
                                for (Map map : list) {
                                    ids.add(MapUtils.getString(map,joinColumnName));
                                }
                                lambdaQuery.in(joinSelfColumn,ids);
                            }
                        }
                        continue;
                    }
                    switch (q.type()) {
                        case EQUAL:
                            lambdaQuery.eq(attributeName,val);
                            break;
                        case GREATER_THAN:
                            lambdaQuery.gt(attributeName,val);
                            break;
                        case LESS_THAN:
                            lambdaQuery.le(attributeName,val);
                            break;
                        case LESS_THAN_NQ:
                            lambdaQuery.lt(attributeName,val);
                            break;
                        case INNER_LIKE:
                            lambdaQuery.like(attributeName,val);
                            break;
                        case LEFT_LIKE:
                            lambdaQuery.likeLeft(attributeName,val);
                            break;
                        case RIGHT_LIKE:
                            lambdaQuery.likeRight(attributeName,val);
                            break;
                        case IN:
                            if (CollUtil.isNotEmpty((Collection<Object>)val)) {
                                lambdaQuery.in(attributeName,(Collection<Object>) val);
                            }
                            break;
                        case NOT_IN:
                            if (CollUtil.isNotEmpty((Collection<Object>)val)) {
                                lambdaQuery.notIn(attributeName,(Collection<Object>) val);
                            }
                            break;
                        case NOT_EQUAL:
                            lambdaQuery.ne(attributeName,val);
                            break;
                        case NOT_NULL:
                            lambdaQuery.isNotNull(attributeName);
                            break;
                        case IS_NULL:
                            lambdaQuery.isNull(attributeName);
                            break;
                        case BETWEEN:
                            List<Object> between = new ArrayList<>((List<Object>)val);
                            lambdaQuery.between(attributeName,between.get(0),between.get(1));
                            break;
                        default: break;
                    }
                }
                field.setAccessible(accessible);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return lambdaQuery;
    }

    private static Field getField(Class aClass,String fieldName){
        Field field = null;
        try {
            field = aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {

        }
        if(ObjectUtil.isNull(field)){
            try {
                field = aClass.getField(fieldName);
            } catch (NoSuchFieldException e) {

            }
        }

        return field;
    }

    private static String toColumnName(String fieldName){
        StringBuffer buffer = new StringBuffer(fieldName);
        for (int i = buffer.length()-1; i >=1 ; i--) {
            if(Character.isUpperCase(buffer.charAt(i))){
                buffer.insert(i,'_');
            }
        }
        return buffer.toString().toLowerCase();
    }

    /**
     * 获取某个Java类字段对应的db字段
     * @param aClass
     * @param javaFieldName
     * @return
     */
    private static String getColumnName(Class<? extends BaseModel> aClass, String javaFieldName){
        String fieldName = javaFieldName;
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(aClass);
        Assert.notNull(columnMap, "can not find lambda cache for this entity [%s]", aClass.getName());
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
                fieldName, aClass.getName());
        return columnCache.getColumnSelect();
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }
}
