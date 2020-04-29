/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.store.pojo.atom

import com.tencent.devops.common.web.annotation.BkField
import com.tencent.devops.common.web.constant.BkStyleEnum
import com.tencent.devops.repository.pojo.enums.VisibilityLevelEnum
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("插件市场工作台-新增插件请求报文体")
data class MarketAtomCreateRequest(
    @ApiModelProperty("项目编码", required = true)
    var projectCode: String,
    @ApiModelProperty("插件代码", required = true)
    @field:BkField(patternStyle = BkStyleEnum.STORE_CODE_STYLE)
    var atomCode: String,
    @ApiModelProperty("插件名称", required = true)
    @field:BkField(patternStyle = BkStyleEnum.STORE_NAME_STYLE)
    var name: String,
    @ApiModelProperty("开发语言", required = true)
    var language: String,
    @ApiModelProperty("认证方式", required = false)
    val authType: String? = null,
    @ApiModelProperty(value = "项目可视范围", required = false)
    val visibilityLevel: VisibilityLevelEnum? = null,
    @ApiModelProperty(value = "插件代码库不开源原因", required = false)
    val privateReason: String? = null
)