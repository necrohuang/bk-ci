USE devops_ci_store;
SET NAMES utf8mb4;

REPLACE INTO `T_BUSINESS_CONFIG`(`BUSINESS`, `FEATURE`, `BUSINESS_VALUE`, `CONFIG_VALUE`, `DESCRIPTION`) VALUES ('BUILD_TYPE', 'defaultBuildType', 'LINUX', 'DOCKER', 'Docker公共构建机');