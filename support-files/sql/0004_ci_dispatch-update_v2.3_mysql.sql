USE devops_ci_dispatch;
SET NAMES utf8mb4;


CREATE TABLE IF NOT EXISTS `T_DISPATCH_PIPELINE_DOCKER_TASK_SIMPLE` (
    `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `PIPELINE_ID` varchar(64) NOT NULL DEFAULT '' COMMENT '流水线ID',
    `VM_SEQ` varchar(64) NOT NULL DEFAULT '' COMMENT '构建机序号',
    `DOCKER_IP` varchar(64) NOT NULL DEFAULT '' COMMENT '构建容器IP',
    `GMT_CREATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `GMT_MODIFIED` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UNI_BUILD_SEQ` (`PIPELINE_ID`,`VM_SEQ`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOCKER构建任务表';

CREATE TABLE IF NOT EXISTS `T_DISPATCH_PIPELINE_DOCKER_TASK_DRIFT` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOCKER构建任务漂移记录表';

CREATE TABLE IF NOT EXISTS `T_DISPATCH_PIPELINE_DOCKER_POOL` (
    `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `PIPELINE_ID` varchar(64) NOT NULL DEFAULT '' COMMENT '流水线ID',
    `VM_SEQ` varchar(64) NOT NULL DEFAULT '' COMMENT '构建机序号',
    `POOL_NO` int(11) NOT NULL DEFAULT 0 COMMENT '构建池序号',
    `STATUS` int(11) NOT NULL DEFAULT 0 COMMENT '构建池状态',
    `GMT_CREATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `GMT_MODIFIED` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UNI_BUILD_SEQ` (`PIPELINE_ID`,`VM_SEQ`, `POOL_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOCKER并发构建池状态表';

CREATE TABLE IF NOT EXISTS `T_DISPATCH_PIPELINE_DOCKER_IP_INFO` (
    `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `DOCKER_IP` varchar(64) NOT NULL DEFAULT '' COMMENT 'DOCKER IP',
    `CAPACITY` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器总容量',
    `USED_NUM` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器已使用容量',
    `CPU_LOAD` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器CPU负载',
    `MEM_LOAD` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器MEM负载',
    `DISK_LOAD` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器DISK负载',
    `DISK_IO_LOAD` int(11) NOT NULL DEFAULT 0 COMMENT '节点容器DISK IO负载',
    `ENABLE` bit(1) DEFAULT 0 COMMENT '节点是否可用',
    `SPECIAL_ON` bit(1) DEFAULT 0 COMMENT '节点是否作为专用机',
    `GRAY_ENV` bit(1) DEFAULT 0 COMMENT '是否为灰度节点',
    `GMT_CREATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `GMT_MODIFIED` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UNI_IP` (`DOCKER_IP`),
    INDEX `idx_1` (`ENABLE`, `GRAY_ENV`, `CPU_LOAD`, `MEM_LOAD`, `DISK_LOAD`, `DISK_IO_LOAD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DOCKER构建机负载表';


DROP PROCEDURE IF EXISTS ci_dispatch_schema_update;

DELIMITER <CI_UBF>

CREATE PROCEDURE ci_dispatch_schema_update()
BEGIN

    DECLARE db VARCHAR(100);
    SET AUTOCOMMIT = 0;
    SELECT DATABASE() INTO db;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_DISPATCH_PIPELINE_DOCKER_DEBUG'
                    AND COLUMN_NAME = 'POOL_NO') THEN
        ALTER TABLE T_DISPATCH_PIPELINE_DOCKER_DEBUG
            ADD COLUMN `POOL_NO` int(11) DEFAULT 0;
    END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_DISPATCH_PIPELINE_DOCKER_BUILD'
                    AND COLUMN_NAME = 'DOCKER_IP') THEN
        ALTER TABLE T_DISPATCH_PIPELINE_DOCKER_BUILD
            ADD COLUMN `DOCKER_IP` VARCHAR(64) DEFAULT '' COMMENT '构建机IP';
    END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_DISPATCH_PIPELINE_DOCKER_BUILD'
                    AND COLUMN_NAME = 'CONTAINER_ID') THEN
        ALTER TABLE T_DISPATCH_PIPELINE_DOCKER_BUILD
            ADD COLUMN `CONTAINER_ID` VARCHAR(128) DEFAULT '' COMMENT '构建容器ID';
    END IF;

    IF NOT EXISTS(SELECT 1
                  FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = db
                    AND TABLE_NAME = 'T_DISPATCH_PIPELINE_DOCKER_BUILD'
                    AND COLUMN_NAME = 'POOL_NO') THEN
        ALTER TABLE T_DISPATCH_PIPELINE_DOCKER_BUILD
            ADD COLUMN `POOL_NO` INT(11) DEFAULT 0 COMMENT '构建容器池序号';
    END IF;

    COMMIT;

END <CI_UBF>
DELIMITER ;
COMMIT;
CALL ci_dispatch_schema_update();