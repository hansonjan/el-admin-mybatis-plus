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
package com.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zheng Jie
 * @date 2019-6-4 13:52:30
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    // Dong ZhaoYang 2017/8/7 基本对象的属性名
    String propName() default "";
    // Dong ZhaoYang 2017/8/7 查询方式
    Type type() default Type.EQUAL;

    /**
     * 连接查询的属性名，如User类中的dept
     */
    String joinName() default "";

    /**
     * 联接表对应的Pojo全类名。如com.admin.modules.system.domain.Dict.java应写成"com.admin.modules.system.domain.Dict"
     * 此参数用于拼接名字从spring中获取相关的对象，如参数值为"dict"
     * 拼接“Mapper"可获得Mapper类： dictMapper->com.admin.modules.system.mapper.dictMapper
     * 拼接“Service"可获得Service类： dictMapper->com.admin.modules.system.service.impl.dictServiceImpl
     * @return
     */
    String joinClassName() default "";

    /**
     * 联表字段名，此处应是java属性名。使用时按驼峰规则自动转换成db字段名进行使用。
     * @return
     */
    String joinFieldName() default "";
    /**
     * 多字段模糊搜索，仅支持String类型字段，多个用逗号隔开, 如@Query(blurry = "email,username")
     */
    String blurry() default "";

    enum Type {
        // jie 2019/6/4 相等
        EQUAL
        // Dong ZhaoYang 2017/8/7 大于等于
        , GREATER_THAN
        // Dong ZhaoYang 2017/8/7 小于等于
        , LESS_THAN
        // Dong ZhaoYang 2017/8/7 中模糊查询
        , INNER_LIKE
        // Dong ZhaoYang 2017/8/7 左模糊查询
        , LEFT_LIKE
        // Dong ZhaoYang 2017/8/7 右模糊查询
        , RIGHT_LIKE
        // Dong ZhaoYang 2017/8/7 小于
        , LESS_THAN_NQ
        // jie 2019/6/4 包含
        , IN
        // 不包含
        , NOT_IN
        // 不等于
        ,NOT_EQUAL
        // between
        ,BETWEEN
        // 不为空
        ,NOT_NULL
        // 为空
        ,IS_NULL
    }

}

