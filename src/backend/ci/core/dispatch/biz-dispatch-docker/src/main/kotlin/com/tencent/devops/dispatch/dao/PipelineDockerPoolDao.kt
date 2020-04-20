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

package com.tencent.devops.dispatch.dao

import com.tencent.devops.model.dispatch.tables.TDispatchPipelineDockerPool
import com.tencent.devops.model.dispatch.tables.records.TDispatchPipelineDockerPoolRecord
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class PipelineDockerPoolDao @Autowired constructor() {
    fun create(
        dslContext: DSLContext,
        pipelineId: String,
        vmSeq: String,
        poolNo: Int,
        status: Int
    ) {
        with(TDispatchPipelineDockerPool.T_DISPATCH_PIPELINE_DOCKER_POOL) {
            dslContext.insertInto(
                this,
                PIPELINE_ID,
                VM_SEQ,
                POOL_NO,
                STATUS,
                GMT_CREATE,
                GMT_MODIFIED
            ).values(
                pipelineId,
                vmSeq,
                poolNo,
                status,
                LocalDateTime.now(),
                LocalDateTime.now()
            ).execute()
        }
    }

    fun getPoolNo(
        dslContext: DSLContext,
        pipelineId: String,
        vmSeq: String,
        poolNo: Int
    ): TDispatchPipelineDockerPoolRecord? {
        with(TDispatchPipelineDockerPool.T_DISPATCH_PIPELINE_DOCKER_POOL) {
            return dslContext.selectFrom(this)
                .where(PIPELINE_ID.eq(pipelineId))
                .and(VM_SEQ.eq(vmSeq))
                .and(POOL_NO.eq(poolNo))
                .fetchOne()
        }
    }

    fun updatePoolStatus(
        dslContext: DSLContext,
        pipelineId: String,
        vmSeq: String,
        poolNo: Int,
        status: Int
    ): Boolean {
        with(TDispatchPipelineDockerPool.T_DISPATCH_PIPELINE_DOCKER_POOL) {
            return dslContext.update(this)
                .set(STATUS, status)
                .where(PIPELINE_ID.eq(pipelineId))
                .and(VM_SEQ.eq(vmSeq))
                .and(POOL_NO.eq(poolNo))
                .execute() == 1
        }
    }
}

/*
CREATE TABLE `T_DISPATCH_PIPELINE_DOCKER_TASK_DRIFT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `PIPELINE_ID` varchar(64) NOT NULL DEFAULT '' COMMENT '流水线ID',
  `BUILD_ID` varchar(64) NOT NULL DEFAULT '' COMMENT '构建ID',
  `VM_SEQ` varchar(64) NOT NULL DEFAULT '' COMMENT '构建机序号',
  `OLD_DOCKER_IP` varchar(64) NOT NULL DEFAULT '' COMMENT '旧构建容器IP',
  `NEW_DOCKER_IP` varchar(64) NOT NULL DEFAULT '' COMMENT '新构建容器IP',
  `OLD_DOCKER_IP_INFO` varchar(1024) NOT NULL DEFAULT '' COMMENT '旧容器IP负载',
  `GMT_CREATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `GMT_MODIFIED` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNI_BUILD_SEQ` (`PIPELINE_ID`,`VM_SEQ`),
  INDEX `IDX_P_B`(`PIPELINE_ID`, `BUILD_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DOCKER构建任务漂移记录表';*/
