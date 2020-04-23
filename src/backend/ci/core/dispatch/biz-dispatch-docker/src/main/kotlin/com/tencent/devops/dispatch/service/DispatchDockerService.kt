package com.tencent.devops.dispatch.service

import com.tencent.devops.dispatch.dao.PipelineDockerIPInfoDao
import com.tencent.devops.dispatch.pojo.DockerHostLoadConfig
import com.tencent.devops.dispatch.pojo.DockerIpInfoVO
import com.tencent.devops.dispatch.pojo.DockerIpListPage
import com.tencent.devops.dispatch.utils.DockerHostUtils
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class DispatchDockerService @Autowired constructor(
    private val dslContext: DSLContext,
    private val pipelineDockerIPInfoDao: PipelineDockerIPInfoDao,
    private val dockerHostUtils: DockerHostUtils
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DispatchDockerService::class.java)
    }

    fun list(userId: String, page: Int?, pageSize: Int?): DockerIpListPage<DockerIpInfoVO> {
        val pageNotNull = page ?: 1
        val pageSizeNotNull = pageSize ?: 10

        try {
            val dockerIpList = pipelineDockerIPInfoDao.getDockerIpList(dslContext, pageNotNull, pageSizeNotNull)
            val count = pipelineDockerIPInfoDao.getDockerIpCount(dslContext)

            if (dockerIpList.size == 0 || count == 0L) {
                return DockerIpListPage(pageNotNull, pageSizeNotNull, 0, emptyList())
            }
            val dockerIpInfoVOList = mutableListOf<DockerIpInfoVO>()
            dockerIpList.forEach {
                dockerIpInfoVOList.add(DockerIpInfoVO(
                    it.id,
                    it.dockerIp,
                    it.dockerHostPort,
                    it.capacity,
                    it.usedNum,
                    it.cpuLoad,
                    it.memLoad,
                    it.diskLoad,
                    it.diskIoLoad,
                    it.enable,
                    it.grayEnv,
                    it.specialOn,
                    it.gmtCreate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ))
            }

            return DockerIpListPage(pageNotNull, pageSizeNotNull, count, dockerIpInfoVOList)
        } catch (e: Exception) {
            logger.error("OP dispatchDocker list error.", e)
            throw RuntimeException("OP dispatchDocker list error.")
        }
    }

    fun create(userId: String, dockerIpInfoVO: DockerIpInfoVO): Boolean {
        logger.info("$userId create IDC IP $dockerIpInfoVO")
        try {
            pipelineDockerIPInfoDao.create(
                dslContext,
                dockerIpInfoVO.dockerIp,
                dockerIpInfoVO.dockerHostPort,
                dockerIpInfoVO.capacity,
                dockerIpInfoVO.usedNum,
                dockerIpInfoVO.averageCpuLoad,
                dockerIpInfoVO.averageMemLoad,
                dockerIpInfoVO.averageDiskLoad,
                dockerIpInfoVO.averageDiskIOLoad,
                dockerIpInfoVO.enable,
                dockerIpInfoVO.grayEnv,
                dockerIpInfoVO.specialOn
            )
            return true
        } catch (e: Exception) {
            logger.error("OP dispatchDocker create error.", e)
            throw RuntimeException("OP dispatchDocker create error.")
        }
    }

    fun update(userId: String, dockerIpInfoId: Long, dockerIpInfoVO: DockerIpInfoVO): Boolean {
        logger.info("$userId update Docker IP id: $dockerIpInfoId dockerIpInfoVO: $dockerIpInfoVO")
        try {
            pipelineDockerIPInfoDao.update(
                dslContext,
                dockerIpInfoVO.dockerIp,
                dockerIpInfoVO.usedNum,
                dockerIpInfoVO.averageCpuLoad,
                dockerIpInfoVO.averageMemLoad,
                dockerIpInfoVO.averageDiskLoad,
                dockerIpInfoVO.averageDiskIOLoad,
                dockerIpInfoVO.enable,
                dockerIpInfoVO.grayEnv,
                dockerIpInfoVO.specialOn
            )
            return true
        } catch (e: Exception) {
            logger.error("OP dispatchDocker update error.", e)
            throw RuntimeException("OP dispatchDocker update error.")
        }
    }

    fun delete(userId: String, dockerIpInfoId: Long): Boolean {
        logger.info("$userId delete $dockerIpInfoId")
        try {
            pipelineDockerIPInfoDao.delete(dslContext, dockerIpInfoId)
            return true
        } catch (e: Exception) {
            logger.error("OP dispatchDocker delete error.", e)
            throw RuntimeException("OP dispatchDocker delete error.")
        }
    }

    fun createDockerHostLoadConfig(
        userId: String,
        dockerHostLoadConfigMap: Map<String, DockerHostLoadConfig>
    ): Boolean {
        logger.info("$userId createDockerHostLoadConfig $dockerHostLoadConfigMap")
        if (dockerHostLoadConfigMap.size != 3) {
            throw RuntimeException("Parameter dockerHostLoadConfigMap`size is not 3.")
        }

        try {
            dockerHostUtils.createLoadConfig(dockerHostLoadConfigMap)
            return true
        } catch (e: Exception) {
            logger.error("OP dispatcheDocker create dockerhost loadConfig error.", e)
            throw RuntimeException("OP dispatcheDocker create dockerhost loadConfig error.")
        }
    }
}