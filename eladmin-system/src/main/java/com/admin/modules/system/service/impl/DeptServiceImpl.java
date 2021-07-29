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
import com.admin.modules.system.domain.Dept;
import com.admin.modules.system.domain.Role;
import com.admin.modules.system.domain.User;
import com.admin.modules.system.mapper.DeptMapper;
import com.admin.modules.system.mapper.RoleMapper;
import com.admin.modules.system.mapper.UserMapper;
import com.admin.modules.system.service.DeptService;
import com.admin.modules.system.service.dto.DeptDto;
import com.admin.modules.system.service.dto.DeptQueryCriteria;
import com.admin.modules.system.service.mapstruct.MapStructDeptMapper;
import com.admin.utils.*;
import com.admin.utils.enums.DataScopeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl extends ServiceImpl<DeptMapper,Dept> implements DeptService {

    private final MapStructDeptMapper mapStructDeptMapper;
    private final RedisUtils redisUtils;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    /**
     * 查找关联字段Roles
     * @param dept
     */
    private void fillUserRelations(Dept dept){
        if(ObjectUtil.isNull(dept)){
            return;
        }
        Set<Role> roles = roleMapper.findByDeptId(dept.getId());
        dept.setRoles(roles);
    }
    /**
     * 查找关联字段Roles
     * @param depts
     */
    private void fillUserRelations(List<Dept> depts){
        if(ObjectUtil.isEmpty(depts)){
            return;
        }
        for (Dept dept : depts) {
            fillUserRelations(dept);
        }
    }

    @Override
    public List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if(dataScopeType.equals(DataScopeEnum.ALL.getValue())){
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>(){{ add("pidIsNull");add("enabled");}};
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if(fieldNames.contains(field.getName())){
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        QueryWrapper<Dept> queryWrapper = (QueryWrapper<Dept>) QueryHelp.getQueryWrapper(criteria, Dept.class);
        queryWrapper.orderByAsc("dept_sort");
        List<Dept> list = this.list(queryWrapper);
        fillUserRelations(list);
        List<DeptDto> collect = list.stream().map(mapStructDeptMapper::toDto).collect(Collectors.toList());
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isBlank(dataScopeType)){
            return deduplication(collect);
        }
        return collect;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public DeptDto findById(Long id) {
        Dept dept = this.getById(id);
        if(ObjectUtil.isNull(dept)){
            dept = new Dept();
        }
        ValidationUtil.isNull(dept.getId(),"Dept","id",id);
        fillUserRelations(dept);
        return mapStructDeptMapper.toDto(dept);
    }

    @Override
    public List<Dept> findByPid(long pid) {
        LambdaQueryWrapper<Dept> lambdaQuery = Wrappers.lambdaQuery(Dept.class);
        lambdaQuery.eq(Dept::getPid,pid);
        List<Dept> list = this.list(lambdaQuery);
        fillUserRelations(list);
        return list;
    }

    @Override
    public Set<Dept> findByRoleId(Long id) {
        return baseMapper.findByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dept resources) {
        this.saveOrUpdate(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dept resources) {
        // 旧的部门
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        Dept dept = this.getById(resources.getId());
        if(ObjectUtil.isNull(dept)){
            dept = new Dept();
        }
        ValidationUtil.isNull( dept.getId(),"Dept","id",resources.getId());
        resources.setId(dept.getId());
        this.saveOrUpdate(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<DeptDto> deptDtos) {
        for (DeptDto deptDto : deptDtos) {
            // 清理缓存
            delCaches(deptDto.getId());
            this.removeById(deptDto.getId());
            updateSubCnt(deptDto.getPid());
        }
    }

    @Override
    public void download(List<DeptDto> deptDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeptDto deptDTO : deptDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部门名称", deptDTO.getName());
            map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Set<DeptDto> getDeleteDepts(List<Dept> menuList, Set<DeptDto> deptDtos) {
        for (Dept dept : menuList) {
            deptDtos.add(mapStructDeptMapper.toDto(dept));
            List<Dept> depts = findByPid(dept.getPid());
            if(depts!=null && depts.size()!=0){
                getDeleteDepts(depts, deptDtos);
            }
        }
        return deptDtos;
    }

    @Override
    public List<Long> getDeptChildren(List<Dept> deptList) {
        List<Long> list = new ArrayList<>();
        deptList.forEach(dept -> {
            if (dept!=null && dept.getEnabled()) {
                List<Dept> depts = findByPid(dept.getPid());
                if (depts.size() != 0) {
                    list.addAll(getDeptChildren(depts));
                }
                list.add(dept.getId());
            }
        });
        return list;
    }

    @Override
    public List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts) {
        if(deptDto.getPid() == null){
            LambdaQueryWrapper<Dept> lambdaQuery = Wrappers.lambdaQuery(Dept.class);
            lambdaQuery.isNull(Dept::getPid);
            List<Dept> list = this.list(lambdaQuery);
            fillUserRelations(list);
            depts.addAll(list);
            return depts.stream().map(mapStructDeptMapper::toDto).collect(Collectors.toList());
        }
        depts.addAll(findByPid(deptDto.getPid()));
        return getSuperior(findById(deptDto.getPid()), depts);
    }

    @Override
    public Object buildTree(List<DeptDto> deptDtos) {
        Set<DeptDto> trees = new LinkedHashSet<>();
        Set<DeptDto> depts= new LinkedHashSet<>();
        List<String> deptNames = deptDtos.stream().map(DeptDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (DeptDto deptDTO : deptDtos) {
            isChild = false;
            if (deptDTO.getPid() == null) {
                trees.add(deptDTO);
            }
            for (DeptDto it : deptDtos) {
                if (it.getPid() != null && deptDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (deptDTO.getChildren() == null) {
                        deptDTO.setChildren(new ArrayList<>());
                    }
                    deptDTO.getChildren().add(it);
                }
            }
            if(isChild) {
                depts.add(deptDTO);
            } else if(deptDTO.getPid() != null &&  !deptNames.contains(findById(deptDTO.getPid()).getName())) {
                depts.add(deptDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",deptDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? deptDtos :trees);
        return map;
    }

    @Override
    public void verification(Set<DeptDto> deptDtos) {
        Set<Long> deptIds = deptDtos.stream().map(DeptDto::getId).collect(Collectors.toSet());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("dept_id", deptIds);
        Integer userCount = userMapper.selectCount(userQueryWrapper);
        if(userCount > 0){
            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
        }

        QueryWrapper<Role> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.in("dept_id", deptIds);
        Integer roleCount = roleMapper.selectCount(roleQueryWrapper);
        if(roleCount > 0){
            throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
        }
    }

    @Override
    public int countByPid(Long pid) {
        LambdaQueryWrapper<Dept> lambdaQuery = Wrappers.lambdaQuery(Dept.class);
        lambdaQuery.eq(Dept::getPid,pid);
        return this.count(lambdaQuery);
    }

    @Override
    public void updateSubCntById(Integer count, Long id) {
        LambdaUpdateWrapper<Dept> lambdaUpdate = Wrappers.lambdaUpdate(Dept.class);
        lambdaUpdate.eq(Dept::getId,id);
        lambdaUpdate.set(Dept::getSubCount,count);
        this.update(lambdaUpdate);
    }

    private void updateSubCnt(Long deptId){
        if(deptId != null){
            int count = countByPid(deptId);
            this.updateSubCntById(count, deptId);
        }
    }

    private List<DeptDto> deduplication(List<DeptDto> list) {
        List<DeptDto> deptDtos = new ArrayList<>();
        for (DeptDto deptDto : list) {
            boolean flag = true;
            for (DeptDto dto : list) {
                if (dto.getId().equals(deptDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                deptDtos.add(deptDto);
            }
        }
        return deptDtos;
    }

    /**
     * 清理缓存
     * @param id /
     */
    public void delCaches(Long id){
        List<User> users = userMapper.findByRoleDeptId(id);
        // 删除数据权限
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);
    }
}
