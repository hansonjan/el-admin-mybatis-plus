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
package com.admin.modules.system.service;

import com.admin.modules.system.domain.Menu;
import com.admin.modules.system.service.dto.MenuDto;
import com.admin.modules.system.service.dto.MenuQueryCriteria;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-12-17
 */
public interface MenuService extends IService<Menu> {

    /**
     * 查询全部数据
     * @param criteria 条件
     * @param isQuery /
     * @throws Exception /
     * @return /
     */
    List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    MenuDto findById(long id);
    /**
     * 根据菜单的 PID 查询
     * @param pid /
     * @return /
     */
    List<Menu> findByPid(long pid);
    /**
     * 查找菜单的 PID 为空的记录
     * @return /
     */
    List<Menu> findByPidIsNull();

    /**
     * 根据菜单标题查询
     * @param title 菜单标题
     * @return
     */
    MenuDto findByTitle(String title);
    /**
     * 根据组件名称查询
     * @param name 组件名称
     * @return
     */
    MenuDto findByComponentName(String name);
    /**
     * 创建
     * @param resources /
     */
    void create(Menu resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(Menu resources);

    /**
     * 获取所有子节点，包含自身ID
     * @param menuList /
     * @param menuSet /
     * @return /
     */
    Set<Menu> getChildMenus(List<Menu> menuList, Set<Menu> menuSet);

    /**
     * 构建菜单树
     * @param menuDtos 原始数据
     * @return /
     */
    List<MenuDto> buildTree(List<MenuDto> menuDtos);

    /**
     * 构建菜单树
     * @param menuDtos /
     * @return /
     */
    Object buildMenus(List<MenuDto> menuDtos);

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    Menu findOne(Long id);

    /**
     * 更新节点数目
     * @param count
     * @param menuId
     */
    void updateSubCntById(int count, Long menuId);
    /**
     * 删除
     * @param menuSet /
     */
    void delete(Set<Menu> menuSet);

    /**
     * 导出
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<MenuDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 懒加载菜单数据
     * @param pid /
     * @return /
     */
    List<MenuDto> getMenus(Long pid);

    /**
     * 根据ID获取同级与上级数据
     * @param menuDto /
     * @param objects /
     * @return /
     */
    List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> objects);

    /**
     * 根据当前用户获取菜单
     * @param currentUserId /
     * @return /
     */
    List<MenuDto> findByUser(Long currentUserId);
}
