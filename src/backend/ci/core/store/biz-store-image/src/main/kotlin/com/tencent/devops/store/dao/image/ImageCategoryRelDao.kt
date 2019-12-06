package com.tencent.devops.store.dao.image

import com.tencent.devops.model.store.tables.TCategory
import com.tencent.devops.model.store.tables.TImageCategoryRel
import com.tencent.devops.store.constant.StoreMessageCode.USER_IMAGE_UNKNOWN_IMAGE_CATEGORY
import com.tencent.devops.store.dao.image.Constants.KEY_CATEGORY_CODE
import com.tencent.devops.store.dao.image.Constants.KEY_CATEGORY_ICON_URL
import com.tencent.devops.store.dao.image.Constants.KEY_CATEGORY_ID
import com.tencent.devops.store.dao.image.Constants.KEY_CATEGORY_NAME
import com.tencent.devops.store.dao.image.Constants.KEY_CATEGORY_TYPE
import com.tencent.devops.store.dao.image.Constants.KEY_CREATE_TIME
import com.tencent.devops.store.dao.image.Constants.KEY_UPDATE_TIME
import com.tencent.devops.store.exception.image.CategoryNotExistException
import com.tencent.devops.store.pojo.image.enums.CategoryTypeEnum
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.Record7
import org.jooq.Result
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ImageCategoryRelDao {
    fun getCategoryCodeByImageId(dslContext: DSLContext, imageId: String): Result<Record1<String>>? {
        val tImageCategoryRel = TImageCategoryRel.T_IMAGE_CATEGORY_REL.`as`("tImageCategoryRel")
        val tCategory = TCategory.T_CATEGORY.`as`("tCategory")
        return dslContext.select(
            tCategory.CATEGORY_CODE.`as`(KEY_CATEGORY_CODE)
        ).from(tImageCategoryRel).join(tCategory)
            .on(tImageCategoryRel.CATEGORY_ID.eq(tCategory.ID))
            .where(tImageCategoryRel.IMAGE_ID.eq(imageId))
            .fetch()
    }

    fun getImageIdsByCategoryIds(
        dslContext: DSLContext,
        categoryIds: Set<String>
    ): Result<Record1<String>>? {
        with(TImageCategoryRel.T_IMAGE_CATEGORY_REL) {
            return dslContext.select(IMAGE_ID).from(this)
                .where(CATEGORY_ID.`in`(categoryIds))
                .fetch()
        }
    }

    fun getCategorysByImageId(
        dslContext: DSLContext,
        imageId: String
    ): Result<Record7<String, String, String, String, Byte, LocalDateTime, LocalDateTime>>? {
        val a = TCategory.T_CATEGORY.`as`("a")
        val b = TImageCategoryRel.T_IMAGE_CATEGORY_REL.`as`("b")
        return dslContext.select(
            a.ID.`as`(KEY_CATEGORY_ID),
            a.CATEGORY_CODE.`as`(KEY_CATEGORY_CODE),
            a.CATEGORY_NAME.`as`(KEY_CATEGORY_NAME),
            a.ICON_URL.`as`(KEY_CATEGORY_ICON_URL),
            a.TYPE.`as`(KEY_CATEGORY_TYPE),
            a.CREATE_TIME.`as`(KEY_CREATE_TIME),
            a.UPDATE_TIME.`as`(KEY_UPDATE_TIME)
        ).from(a).join(b).on(a.ID.eq(b.CATEGORY_ID))
            .where(b.IMAGE_ID.eq(imageId))
            .fetch()
    }

    fun deleteByImageId(dslContext: DSLContext, imageId: String) {
        with(TImageCategoryRel.T_IMAGE_CATEGORY_REL) {
            dslContext.deleteFrom(this)
                .where(IMAGE_ID.eq(imageId))
                .execute()
        }
    }

    fun batchAdd(dslContext: DSLContext, userId: String, imageId: String, categoryIdList: List<String>) {
        with(TImageCategoryRel.T_IMAGE_CATEGORY_REL) {
            val addStep = categoryIdList.map {
                dslContext.insertInto(
                    this,
                    ID,
                    IMAGE_ID,
                    CATEGORY_ID,
                    CREATOR,
                    MODIFIER
                )
                    .values(
                        com.tencent.devops.common.api.util.UUIDUtil.generate(),
                        imageId,
                        it,
                        userId,
                        userId
                    )
            }
            dslContext.batch(addStep).execute()
        }
    }

    fun updateCategory(dslContext: DSLContext, userId: String, imageId: String, categoryCode: String?) {
        if (!categoryCode.isNullOrBlank()) {
            dslContext.transaction { configuration ->
                val context = DSL.using(configuration)
                // 根据categoryCode查出对应的ID
                val tCategory = TCategory.T_CATEGORY.`as`("tCategory")
                val categoryIdRecords = context.select(tCategory.ID).from(tCategory).where(
                    tCategory.CATEGORY_CODE.eq(categoryCode)
                        .and(tCategory.TYPE.eq(CategoryTypeEnum.IMAGE.type.toByte()))
                ).fetch()
                if (categoryIdRecords.size == 0) {
                    throw CategoryNotExistException(
                        message = "category not exist,categoryCode=$categoryCode",
                        errorCode = USER_IMAGE_UNKNOWN_IMAGE_CATEGORY,
                        params = arrayOf(categoryCode ?: "")
                    )
                }
                val categoryId = categoryIdRecords[0].get(0) as String
                deleteByImageId(context, imageId)
                batchAdd(context, userId, imageId, listOf(categoryId))
            }
        }
    }
}