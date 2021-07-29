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
package com.admin.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.admin.base.BaseModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 支付宝配置类
 * @author Zheng Jie
 * @date 2018-12-31
 */
@Data
@TableName("tool_alipay_config")
public class AlipayConfig  extends BaseModel implements Serializable {

    @TableId(value="config_id",type= IdType.AUTO)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "应用ID")
    @TableField("app_id")
    private String appId;

    @NotBlank
    @ApiModelProperty(value = "商户私钥")
    @TableField("private_key")
    private String privateKey;

    @NotBlank
    @ApiModelProperty(value = "支付宝公钥")
    @TableField("public_key")
    private String publicKey;

    @ApiModelProperty(value = "签名方式")
    @TableField("sign_type")
    private String signType="RSA2";

    @ApiModelProperty(value = "支付宝开放安全地址", hidden = true)
    @TableField("gateway_url")
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    @ApiModelProperty(value = "编码", hidden = true)
    @TableField("charset")
    private String charset= "utf-8";

    @NotBlank
    @ApiModelProperty(value = "异步通知地址")
    @TableField("notify_url")
    private String notifyUrl;

    @NotBlank
    @ApiModelProperty(value = "订单完成后返回的页面")
    @TableField("return_url")
    private String returnUrl;

    @ApiModelProperty(value = "类型")
    @TableField("format")
    private String format="JSON";

    @NotBlank
    @ApiModelProperty(value = "商户号")
    @TableField("sys_service_provider_id")
    private String sysServiceProviderId;

}
