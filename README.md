<h1 style="text-align: center">EL-ADMIN-MYBATIS-PLUS 后台管理系统</h1>
<div style="text-align: center">

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/elunez/eladmin/blob/master/LICENSE)
[![star](https://gitee.com/elunez/eladmin/badge/star.svg?theme=white)](https://gitee.com/elunez/eladmin)
[![GitHub stars](https://img.shields.io/github/stars/elunez/eladmin.svg?style=social&label=Stars)](https://github.com/elunez/eladmin)
[![GitHub forks](https://img.shields.io/github/forks/elunez/eladmin.svg?style=social&label=Fork)](https://github.com/elunez/eladmin)

</div>

#### 项目简介
此项目是一个基于eladmin分支改版的后台管理系统。eladmin项目地址如下：

|     |   后端源码  |   前端源码  |
|---  |--- | --- |
|  github   |  https://github.com/elunez/eladmin   |  https://github.com/elunez/eladmin-web   |
|  码云   |  https://gitee.com/elunez/eladmin   |  https://gitee.com/elunez/eladmin-web   |

此项目主要将eladmin中的Jpa修改成了mybatis-plus。另外针对使用体验做了一些优化。
此项目是基于Spring Boot 2.1.0 、 Spring Boot、MyBatis-Plus、JWT、Spring Security、Redis、Vue2的前后端分离的后台管理系统。

与原始el-admin相比，主要修改内容如下：
1、java包名：me.zhengjie修改为com.admin
2、xxxRepository统一更名为xxxMapper
3、MapStruct相关的类全部更名为MapStructXxxMapper
4、根据mybatis-plus的约定：
   一张数据表对应一个：Java类(POJO)、Mapper、Service、ServiceImpl。
       其中Mapper统一继承com.baomidou.mybatisplus.core.mapper.BaseMapper并在类的上方打上注解：org.apache.ibatis.annotations.Mapper;org.springframework.stereotype.Component;
5、Controller的入参Pageable类统一改为IPage。参数自动解析注入见com.admin.convert.PageResolver.java
6、POJO类删除关联对象(POJO嵌套关联类，使用时自动查询是hibernate和jpa的用法，mybatis-plus只保持与原表一致的字段)。
   若pojo中包含关联对象属性，使用@TableField(exist = false)标记其不是表的字段。
7、弃用所有javax.persistence包，这个包主要提供数据表相关的注解，如@Id，@Join等。
   数据表相关的标记，改用com.baomidou.mybatisplus.annotation包下的注解。
8、强调说明，数据表对应的pojo主键字段用@TableId进行标记，非主键字段全部要打上@TableField("{数据库字段名}")进行标记。并且eladmin-system的.yml文件中必须设置mybatis-plus.configuration.map-underscore-to-camel-case=false
   意思是弃用下划线自动转驼峰规则，改用手工指定表字段名的方式。
   这样修改的原因：eladmin将大部分的pojo的主键用id做统一字段名，因为vue前端数据操作使用id做主键。例如role的主键是roleId但在pojo里字段名为id。
   而mybatis-plus的map-underscore-to-camel-case规则比较死板，它不会判断有@TableId或@TableField就用标记指定的字段，没有标记的就按驼峰规则。
   若开启这个参数，则所有字段都按驼峰规则，此时生成的crud语句会变成如insert into role(id,... 实际上应该是insert into role(role_id,...

**开发文档：**  [https://el-admin.vip](https://el-admin.vip)

**体验地址：**  [https://el-admin.xin](https://el-admin.xin)

**账号密码：** `admin / 123456`

#### 项目源码

|     |   后端源码  |   前端源码  |
|---  |--- | --- |
|  github   |  https://github.com/hansonjan/el-admin-mybatis-plus   |  https://github.com/hansonjan/el-admin-vue2   |
|  码云   |  https://gitee.com/hansonjan/el-admin-mybatis-plus   |  https://gitee.com/hansonjan/el-admin-vue2   |

#### 主要特性
- 使用最新技术栈，社区资源丰富。
- 高效率开发，代码生成器可一键生成前后端代码
- 支持数据字典，可方便地对一些状态进行管理
- 支持接口限流，避免恶意请求导致服务层压力过大
- 支持接口级别的功能权限与数据权限，可自定义操作
- 自定义权限注解与匿名接口注解，可快速对接口拦截与放行
- 对一些常用地前端组件封装：表格数据请求、数据字典等
- 前后端统一异常拦截处理，统一输出异常，避免繁琐的判断
- 支持在线用户管理与服务器性能监控，支持限制单用户登录
- 支持运维管理，可方便地对远程服务器的应用进行部署与管理

####  系统功能
- 用户管理：提供用户的相关配置，新增用户后，默认密码为123456
- 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
- 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
- 部门管理：可配置系统组织架构，树形表格展示
- 岗位管理：配置各个部门的职位
- 字典管理：可维护常用一些固定的数据，如：状态，性别等
- 系统日志：记录用户操作日志与异常日志，方便开发人员定位排错
- SQL监控：采用druid 监控数据库访问性能，默认用户名admin，密码123456
- 定时任务：整合Quartz做定时任务，加入任务日志，任务运行情况一目了然
- 代码生成：高灵活度生成前后端代码，减少大量重复的工作任务
- 邮件工具：配合富文本，发送html格式的邮件
- 七牛云存储：可同步七牛云存储的数据到系统，无需登录七牛云直接操作云数据
- 支付宝支付：整合了支付宝支付并且提供了测试账号，可自行测试
- 服务监控：监控服务器的负载情况
- 运维管理：一键部署你的应用

#### 项目结构
项目采用按功能分模块的开发方式，结构如下

- `eladmin-common` 为系统的公共模块，各种工具类，公共配置存在该模块

- `eladmin-system` 为系统核心模块也是项目入口模块，也是最终需要打包部署的模块

- `eladmin-logging` 为系统的日志模块，其他模块如果需要记录日志需要引入该模块

- `eladmin-tools` 为第三方工具模块，包含：图床、邮件、云存储、本地存储、支付宝

- `eladmin-generator` 为系统的代码生成模块，代码生成的模板在 system 模块中

#### 详细结构

```
- eladmin-common 公共模块
    - annotation 为系统自定义注解
    - aspect 自定义注解的切面
    - base 提供了Entity、DTO基类和mapstruct的通用mapper
    - config 自定义权限实现、redis配置、swagger配置、Rsa配置等
    - exception 项目统一异常的处理
    - utils 系统通用工具类
- eladmin-system 系统核心模块（系统启动入口）
	- config 配置跨域与静态资源，与数据权限
	    - thread 线程池相关
	- modules 系统相关模块(登录授权、系统监控、定时任务、运维管理等)
- eladmin-logging 系统日志模块
- eladmin-tools 系统第三方工具模块
- eladmin-generator 系统代码生成模块
```

#### 特别鸣谢

- 感谢 [JetBrains](https://www.jetbrains.com/) 提供的非商业开源软件开发授权

- 感谢 [七牛云](https://www.qiniu.com/) 提供的免费云存储与CDN加速支持

- 感谢 [PanJiaChen](https://github.com/PanJiaChen/vue-element-admin) 大佬提供的前端模板

- 感谢 [Moxun](https://github.com/moxun1639) 大佬提供的前端 Curd 通用组件

- 感谢 [zhy6599](https://gitee.com/zhy6599) 大佬提供的后端运维管理相关功能

- 感谢 [j.yao.SUSE](https://github.com/everhopingandwaiting) 大佬提供的匿名接口与Redis限流等功能

- 感谢 [d15801543974](https://github.com/d15801543974) 大佬提供的基于注解的通用查询方式

#### 项目捐赠
项目的发展离不开你的支持，请作者喝杯咖啡吧☕  [Donate](https://el-admin.vip/donation/)

#### 反馈交流
- QQ交流群：一群：<strike>891137268</strike> 已满、二群：947578238
