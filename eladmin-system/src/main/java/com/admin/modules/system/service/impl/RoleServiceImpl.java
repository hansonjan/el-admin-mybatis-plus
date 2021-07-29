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
package com.admin.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.admin.exception.BadRequestException;
import com.admin.exception.EntityExistException;
import com.admin.modules.security.service.UserCacheClean;
import com.admin.modules.system.domain.Menu;
import com.admin.modules.system.domain.Role;
import com.admin.modules.system.domain.User;
import com.admin.modules.system.mapper.RoleMapper;
import com.admin.modules.system.mapper.UserMapper;
import com.admin.modules.system.service.RoleService;
import com.admin.modules.system.service.dto.RoleDto;
import com.admin.modules.system.service.dto.RoleQueryCriteria;
import com.admin.modules.system.service.dto.RoleSmallDto;
import com.admin.modules.system.service.dto.UserDto;
import com.admin.modules.system.service.mapstruct.MapStructRoleMapper;
import com.admin.modules.system.service.mapstruct.MapStructRoleSmallMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-12-03
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "role")
public class RoleServiceImpl extends ServiceImpl<RoleMapper,Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final MapStructRoleSmallMapper mapStructRoleSmallMapper;
    private final MapStructRoleMapper mapStructRoleMapper;
    private final RedisUtils redisUtils;
    private final UserMapper userMapper;
    private final UserCacheClean userCacheClean;

    @Override
    public List<RoleDto> queryAll() {
        LambdaQueryWrapper<Role> lambdaQuery = Wrappers.lambdaQuery(Role.class);
        lambdaQuery.orderByAsc(Role::getLevel);
        return mapStructRoleMapper.toDto(list(lambdaQuery));
    }

    @Override
    public Object queryAll(RoleQueryCriteria criteria, IPage pageable) {
        QueryWrapper<Role> queryWrapper = (QueryWrapper<Role>) QueryHelp.getQueryWrapper(criteria, Role.class);
        IPage<Role> page = this.page(pageable, queryWrapper);
        List<RoleDto> collect = page.getRecords().stream().map(mapStructRoleMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<RoleDto> queryAll(RoleQueryCriteria criteria) {
        QueryWrapper<Role> queryWrapper = (QueryWrapper<Role>) QueryHelp.getQueryWrapper(criteria, Role.class);
        return mapStructRoleMapper.toDto(list(queryWrapper));
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public RoleDto findById(long id) {
        Role role = baseMapper.selectById(id);
        if(ObjectUtil.isNull(role)){
            role = new Role();
        }
        ValidationUtil.isNull(role.getId(), "Role", "id", id);
        return mapStructRoleMapper.toDto(role);
    }

    @Override
    public RoleDto findByName(String name) {
        LambdaQueryWrapper<Role> lambdaQuery = Wrappers.lambdaQuery(Role.class);
        lambdaQuery.eq(Role::getName,name);
        return mapStructRoleMapper.toDto(baseMapper.selectOne(lambdaQuery));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Role resources) {
        if (findByName(resources.getName()) != null) {
            throw new EntityExistException(Role.class, "username", resources.getName());
        }
        this.saveOrUpdate(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Role resources) {
        Role role = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNull(role)){
            role = new Role();
        }
        ValidationUtil.isNull(role.getId(), "Role", "id", resources.getId());

        RoleDto role1 = findByName(resources.getName());

        if (role1 != null && !role1.getId().equals(role.getId())) {
            throw new EntityExistException(Role.class, "username", resources.getName());
        }
        role.setName(resources.getName());
        role.setDescription(resources.getDescription());
        role.setDataScope(resources.getDataScope());
        role.setDepts(resources.getDepts());
        role.setLevel(resources.getLevel());
        this.saveOrUpdate(role);
        // 更新相关缓存
        delCaches(role.getId(), null);
    }

    @Override
    public void updateMenu(Role resources, RoleDto roleDTO) {
        Role role = mapStructRoleMapper.toEntity(roleDTO);
        List<User> users = userMapper.findByRoleId(role.getId());
        // 更新菜单
        role.setMenus(resources.getMenus());
        delCaches(resources.getId(), users);
        this.saveOrUpdate(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void untiedMenu(Long menuId) {
        // 更新菜单
        roleMapper.untiedMenu(menuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 更新相关缓存
            delCaches(id, null);
        }
        roleMapper.deleteBatchIds(ids);
    }

    @Override
    public List<RoleSmallDto> findByUsersId(Long id) {
        Set<Role> sets = roleMapper.findByUserId(id);
        return mapStructRoleSmallMapper.toDto(new ArrayList<>(sets));
    }

    @Override
    public Integer findByRoles(Set<Role> roles) {
        if (roles.size() == 0) {
            return Integer.MAX_VALUE;
        }
        Set<RoleDto> roleDtos = new HashSet<>();
        for (Role role : roles) {
            roleDtos.add(findById(role.getId()));
        }
        return Collections.min(roleDtos.stream().map(RoleDto::getLevel).collect(Collectors.toList()));
    }

    @Override
    @Cacheable(key = "'auth:' + #p0.id")
    public List<GrantedAuthority> mapToGrantedAuthorities(UserDto user) {
        Set<String> permissions = new HashSet<>();
        // 如果是管理员直接返回
        if (user.getIsAdmin()) {
            permissions.add("admin");
            return permissions.stream().map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        Set<Role> roles = roleMapper.findByUserId(user.getId());
        permissions = roles.stream().flatMap(role -> role.getMenus().stream())
                .filter(menu -> StringUtils.isNotBlank(menu.getPermission()))
                .map(Menu::getPermission).collect(Collectors.toSet());
        return permissions.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public void download(List<RoleDto> roles, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoleDto role : roles) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("角色名称", role.getName());
            map.put("角色级别", role.getLevel());
            map.put("描述", role.getDescription());
            map.put("创建日期", role.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if (userMapper.countByRoles(ids) > 0) {
            throw new BadRequestException("所选角色存在用户关联，请解除关联再试！");
        }
    }

    @Override
    public List<Role> findInMenuId(List<Long> menuIds) {
        return baseMapper.findInMenuId(menuIds);
    }

    /**
     * 清理缓存
     * @param id /
     */
    public void delCaches(Long id, List<User> users) {
        users = CollectionUtil.isEmpty(users) ? userMapper.findByRoleId(id) : users;
        if (CollectionUtil.isNotEmpty(users)) {
            users.forEach(item -> userCacheClean.cleanUserCache(item.getUsername()));
            Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
            redisUtils.delByKeys(CacheKey.DATA_USER, userIds);
            redisUtils.delByKeys(CacheKey.MENU_USER, userIds);
            redisUtils.delByKeys(CacheKey.ROLE_AUTH, userIds);
        }
        redisUtils.del(CacheKey.ROLE_ID + id);
    }
}
