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

import cn.hutool.core.util.ObjectUtil;
import com.admin.config.FileProperties;
import com.admin.exception.BadRequestException;
import com.admin.exception.EntityExistException;
import com.admin.exception.EntityNotFoundException;
import com.admin.modules.security.service.OnlineUserService;
import com.admin.modules.security.service.UserCacheClean;
import com.admin.modules.system.domain.Dept;
import com.admin.modules.system.domain.Job;
import com.admin.modules.system.domain.Role;
import com.admin.modules.system.domain.User;
import com.admin.modules.system.mapper.DeptMapper;
import com.admin.modules.system.mapper.JobMapper;
import com.admin.modules.system.mapper.RoleMapper;
import com.admin.modules.system.mapper.UserMapper;
import com.admin.modules.system.service.UserService;
import com.admin.modules.system.service.dto.JobSmallDto;
import com.admin.modules.system.service.dto.RoleSmallDto;
import com.admin.modules.system.service.dto.UserDto;
import com.admin.modules.system.service.dto.UserQueryCriteria;
import com.admin.modules.system.service.mapstruct.MapStructUserMapper;
import com.admin.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    private final MapStructUserMapper mapStructUserMapper;
    private final FileProperties properties;
    private final RedisUtils redisUtils;
    private final UserCacheClean userCacheClean;
    private final OnlineUserService onlineUserService;
    private final RoleMapper roleMapper;
    private final JobMapper jobMapper;
    private final DeptMapper deptMapper;
    /**
     * 查找关联字段Dept,Roles,Jobs
     * @param user
     */
    private void fillUserRelations(User user){
        if(ObjectUtil.isNull(user)){
            return;
        }
        if(ObjectUtil.isNotNull(user.getDeptId())){
            Dept dept = deptMapper.selectById(user.getDeptId());
            user.setDept(dept);
        }
        Set<Role> roles = roleMapper.findByUserId(user.getId());
        user.setRoles(roles);
        Set<Job> jobs = jobMapper.findByUserId(user.getId());
        user.setJobs(jobs);
    }
    /**
     * 查找关联字段Dept,Roles,Jobs
     * @param users
     */
    private void fillUserRelations(List<User> users){
        if(ObjectUtil.isEmpty(users)){
            return;
        }
        for (User user : users) {
            fillUserRelations(user);
        }
    }

    @Override
    public Object queryAll(UserQueryCriteria criteria, IPage pageable) {
        QueryWrapper<User> queryWrapper = (QueryWrapper<User>) QueryHelp.getQueryWrapper(criteria, User.class);
        IPage<User> page = this.page(pageable, queryWrapper);
        fillUserRelations(page.getRecords());
        List<UserDto> collect = page.getRecords().stream().map(mapStructUserMapper::toDto).collect(Collectors.toList());
        return PageUtil.toPage(page,collect);
    }

    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        QueryWrapper<User> queryWrapper = (QueryWrapper<User>) QueryHelp.getQueryWrapper(criteria, User.class);
        List<User> list = this.list(queryWrapper);
        fillUserRelations(list);
        return mapStructUserMapper.toDto(list);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(long id) {
        User user = baseMapper.selectById(id);
        if(ObjectUtil.isNull(user)){
            user = new User();
        }
        ValidationUtil.isNull(user.getId(), "User", "id", id);
        fillUserRelations(user);
        return mapStructUserMapper.toDto(user);
    }

    @Override
    public User findByUsername(String name) {
        LambdaQueryWrapper<User> lambdaQuery = Wrappers.lambdaQuery(User.class);
        lambdaQuery.eq(User::getUsername,name);
        User user = baseMapper.selectOne(lambdaQuery);
        fillUserRelations(user);
        return user;
    }

    @Override
    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> lambdaQuery = Wrappers.lambdaQuery(User.class);
        lambdaQuery.eq(User::getPhone,phone);
        User user = baseMapper.selectOne(lambdaQuery);
        fillUserRelations(user);
        return user;
    }

    @Override
    public User findByEmail(String email) {
        LambdaQueryWrapper<User> lambdaQuery = Wrappers.lambdaQuery(User.class);
        lambdaQuery.eq(User::getEmail,email);
        User user = baseMapper.selectOne(lambdaQuery);
        fillUserRelations(user);
        return user;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User resources) {
        if (findByUsername(resources.getUsername()) != null) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (findByEmail(resources.getEmail()) != null) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        if (findByPhone(resources.getPhone()) != null) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }

        if(ObjectUtil.isNotNull(resources.getDept())) {
            resources.setDeptId(resources.getDept().getId());
        }
        this.saveOrUpdate(resources);
        if(ObjectUtil.isNotEmpty(resources.getRoles())){
            baseMapper.addUserRoles(resources.getId(),resources.getRoles());
        }
        if(ObjectUtil.isNotEmpty(resources.getJobs())){
            baseMapper.addUserJobs(resources.getId(),resources.getJobs());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) throws Exception {
        User user = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNull(user)){
            user = new User();
        }
        ValidationUtil.isNull(user.getId(), "User", "id", resources.getId());
        User user1 = findByUsername(resources.getUsername());
        User user2 = findByEmail(resources.getEmail());
        User user3 = findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (user2 != null && !user.getId().equals(user2.getId())) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        if (user3 != null && !user.getId().equals(user3.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        Set<Role> userRoles = roleMapper.findByUserId(user.getId());
        // 如果用户的角色改变
        if (!resources.getRoles().equals(userRoles)) {
            redisUtils.del(CacheKey.DATA_USER + resources.getId());
            redisUtils.del(CacheKey.MENU_USER + resources.getId());
            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
        }
        // 如果用户被禁用，则清除用户登录信息
        if(!resources.getEnabled()){
            onlineUserService.kickOutForUsername(resources.getUsername());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());

        //重建关联记录。
        baseMapper.deleteUserJobs(user.getId());
        baseMapper.deleteUserRoles(user.getId());
        baseMapper.addUserJobs(user.getId(), resources.getJobs());
        baseMapper.addUserRoles(user.getId(),resources.getRoles());
        if(ObjectUtil.isNotNull(resources.getDept())){
            user.setDeptId(resources.getDept().getId());
        }else{
            user.setDeptId(null);
        }

        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        user.setGender(resources.getGender());
        this.saveOrUpdate(user);
        // 清除缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User resources) {
        User user = baseMapper.selectById(resources.getId());
        if(ObjectUtil.isNull(user)){
            user = new User();
        }else{
            fillUserRelations(user);
        }
        User user1 = findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        user.setGender(resources.getGender());
        this.saveOrUpdate(user);
        // 清理缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 清理缓存
            UserDto user = findById(id);
            delCaches(user.getId(), user.getUsername());
        }
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public UserDto findByName(String userName) {
        User user = findByUsername(userName);
        if (user == null) {
            throw new EntityNotFoundException(User.class, "name", userName);
        }
        return mapStructUserMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        baseMapper.updatePass(username, pass, new Timestamp(System.currentTimeMillis()));
        flushCache(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
        // 文件大小验证
        FileUtil.checkSize(properties.getAvatarMaxSize(), multipartFile.getSize());
        // 验证文件上传的格式
        String image = "gif jpg png jpeg";
        String fileType = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        if(fileType != null && !image.contains(fileType)){
            throw new BadRequestException("文件格式错误！, 仅支持 " + image +" 格式");
        }
        User user = findByUsername(SecurityUtils.getCurrentUsername());
        String oldPath = user.getAvatarPath();
        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        user.setAvatarName(file.getName());
        this.saveOrUpdate(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        @NotBlank String username = user.getUsername();
        flushCache(username);
        return new HashMap<String, String>(1) {{
            put("avatar", file.getName());
        }};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        baseMapper.updateEmail(username, email);
        flushCache(username);
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("角色", roles);
            map.put("部门", userDTO.getDept().getName());
            map.put("岗位", userDTO.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("修改密码的时间", userDTO.getPwdResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 清理缓存
     *
     * @param id /
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        flushCache(username);
    }

    /**
     * 清理 登陆时 用户缓存信息
     *
     * @param username /
     */
    private void flushCache(String username) {
        userCacheClean.cleanUserCache(username);
    }
}
