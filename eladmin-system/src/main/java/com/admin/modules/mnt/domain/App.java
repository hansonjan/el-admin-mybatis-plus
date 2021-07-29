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
package com.admin.modules.mnt.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.admin.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
* @author zhanghouying
* @date 2019-08-24
*/
@Getter
@Setter
@TableName("mnt_app")
public class App extends BaseEntity implements Serializable {

	@TableId(value="app_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
	private Long id;

	@ApiModelProperty(value = "名称")
    @TableField("name")
    private String name;

	@ApiModelProperty(value = "端口")
	@TableField("port")
	private int port;

	@ApiModelProperty(value = "上传路径")
	@TableField("upload_path")
	private String uploadPath;

	@ApiModelProperty(value = "部署路径")
	@TableField("deploy_path")
	private String deployPath;

	@ApiModelProperty(value = "备份路径")
	@TableField("backup_path")
	private String backupPath;

	@ApiModelProperty(value = "启动脚本")
	@TableField("start_script")
	private String startScript;

	@ApiModelProperty(value = "部署脚本")
	@TableField("deploy_script")
	private String deployScript;

    public void copy(App source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
