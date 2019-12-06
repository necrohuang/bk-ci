package com.tencent.devops.store.dao.image

import com.tencent.devops.model.store.tables.TImage
import com.tencent.devops.model.store.tables.TImageFeature
import com.tencent.devops.model.store.tables.TStoreProjectRel
import com.tencent.devops.model.store.tables.records.TImageFeatureRecord
import com.tencent.devops.store.dao.image.Constants.KEY_IMAGE_CODE
import com.tencent.devops.store.pojo.common.enums.StoreProjectTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.pojo.image.enums.ImageRDTypeEnum
import com.tencent.devops.store.pojo.image.enums.ImageStatusEnum
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.Result
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ImageFeatureDao {
    fun getImageFeature(dslContext: DSLContext, imageCode: String): TImageFeatureRecord? {
        with(TImageFeature.T_IMAGE_FEATURE) {
            return dslContext.selectFrom(this)
                .where(IMAGE_CODE.eq(imageCode))
                .fetchOne()
        }
    }

    fun deleteAll(dslContext: DSLContext, imageCode: String): Int {
        with(TImageFeature.T_IMAGE_FEATURE) {
            return dslContext.deleteFrom(this)
                .where(IMAGE_CODE.eq(imageCode))
                .execute()
        }
    }

    /**
     * 根据imageCode更新ImageFeature表中其余字段
     */
    fun update(
        dslContext: DSLContext,
        imageCode: String,
        publicFlag: Boolean?,
        recommendFlag: Boolean?,
        certificationFlag: Boolean?,
        rdType: ImageRDTypeEnum?,
        modifier: String?,
        weight: Int? = null
    ): Int {
        with(TImageFeature.T_IMAGE_FEATURE) {
            var baseQuery = dslContext.update(this).set(UPDATE_TIME, LocalDateTime.now())
            if (publicFlag != null) {
                baseQuery = baseQuery.set(PUBLIC_FLAG, publicFlag)
            }
            if (recommendFlag != null) {
                baseQuery = baseQuery.set(RECOMMEND_FLAG, recommendFlag)
            }
            if (certificationFlag != null) {
                baseQuery = baseQuery.set(CERTIFICATION_FLAG, certificationFlag)
            }
            if (rdType != null) {
                baseQuery = baseQuery.set(IMAGE_TYPE, rdType.type.toByte())
            }
            if (!modifier.isNullOrBlank()) {
                baseQuery = baseQuery.set(MODIFIER, modifier)
            }
            if (weight != null) {
                baseQuery = baseQuery.set(WEIGHT, weight)
            }
            return baseQuery.where(IMAGE_CODE.eq(imageCode)).execute()
        }
    }

    /**
     * 带offset与limit查询公共非调试镜像代码
     */
    fun getPublicImageCodes(
        dslContext: DSLContext,
        projectCode: String,
        imageStatusSet: Set<ImageStatusEnum>?,
        offset: Int? = 0,
        limit: Int? = -1
    ): Result<Record1<String>>? {
        val tStoreProjectRel = TStoreProjectRel.T_STORE_PROJECT_REL.`as`("tStoreProjectRel")
        val tImageFeature = TImageFeature.T_IMAGE_FEATURE.`as`("tImageFeature")
        val tImage = TImage.T_IMAGE.`as`("tImage")
        // 先查出项目的调试项目
        val debugImageCodes = dslContext.select(tStoreProjectRel.STORE_CODE).from(tStoreProjectRel)
            .where(tStoreProjectRel.PROJECT_CODE.eq(projectCode))
            .and(tStoreProjectRel.TYPE.eq(StoreProjectTypeEnum.TEST.type.toByte()))
            .and(tStoreProjectRel.STORE_TYPE.eq(StoreTypeEnum.IMAGE.type.toByte()))
            .fetch()
        val conditions = mutableListOf<Condition>()
        // 镜像
        conditions.add(tStoreProjectRel.STORE_TYPE.eq(StoreTypeEnum.IMAGE.type.toByte()))
        // 公共
        conditions.add(tImageFeature.PUBLIC_FLAG.eq(true))
        // 非调试
        conditions.add(tStoreProjectRel.STORE_CODE.notIn(debugImageCodes))
        if (imageStatusSet != null && imageStatusSet.isNotEmpty()) {
            conditions.add(tImage.IMAGE_STATUS.`in`(imageStatusSet.map { it.status.toByte() }))
        }
        val baseQuery =
            dslContext.selectDistinct(tImageFeature.IMAGE_CODE.`as`(KEY_IMAGE_CODE)).from(tImageFeature).join(tImage)
                .on(tImageFeature.IMAGE_CODE.eq(tImage.IMAGE_CODE))
                .join(tStoreProjectRel).on(tImageFeature.IMAGE_CODE.eq(tStoreProjectRel.STORE_CODE))
                .where(conditions)
        if (offset != null && offset >= 0) {
            baseQuery.offset(offset)
        }
        if (limit != null && limit > 0) {
            baseQuery.limit(limit)
        }
        return baseQuery.fetch()
    }

    fun countPublicImageCodes(
        dslContext: DSLContext,
        projectCode: String,
        imageStatusSet: Set<ImageStatusEnum>?
    ): Int {
        val tStoreProjectRel = TStoreProjectRel.T_STORE_PROJECT_REL.`as`("tStoreProjectRel")
        val tImageFeature = TImageFeature.T_IMAGE_FEATURE.`as`("tImageFeature")
        val tImage = TImage.T_IMAGE.`as`("tImage")
        // 先查出项目的调试项目
        val debugImageCodes = dslContext.select(tStoreProjectRel.STORE_CODE).from(tStoreProjectRel)
            .where(tStoreProjectRel.PROJECT_CODE.eq(projectCode))
            .and(tStoreProjectRel.TYPE.eq(StoreProjectTypeEnum.TEST.type.toByte()))
            .and(tStoreProjectRel.STORE_TYPE.eq(StoreTypeEnum.IMAGE.type.toByte()))
            .fetch()
        val conditions = mutableListOf<Condition>()
        // 镜像
        conditions.add(tStoreProjectRel.STORE_TYPE.eq(StoreTypeEnum.IMAGE.type.toByte()))
        // 公共
        conditions.add(tImageFeature.PUBLIC_FLAG.eq(true))
        // 非调试
        conditions.add(tStoreProjectRel.STORE_CODE.notIn(debugImageCodes))
        if (imageStatusSet != null && imageStatusSet.isNotEmpty()) {
            conditions.add(tImage.IMAGE_STATUS.`in`(imageStatusSet.map { it.status.toByte() }))
        }
        val baseQuery =
            dslContext.select(tImageFeature.IMAGE_CODE.countDistinct()).from(tImageFeature).join(tImage)
                .on(tImageFeature.IMAGE_CODE.eq(tImage.IMAGE_CODE))
                .join(tStoreProjectRel).on(tImageFeature.IMAGE_CODE.eq(tStoreProjectRel.STORE_CODE))
                .where(conditions)
        return baseQuery.fetchOne().get(0, Int::class.java)
    }
}