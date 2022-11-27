package com.kuluruvineeth.repository.activity

import com.kuluruvineeth.data.models.Activity
import com.kuluruvineeth.data.responses.ActivityResponse
import com.kuluruvineeth.util.Constants

interface ActivityRepository {

    suspend fun getActivitiesForUser(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_ACTIVITY_PAGE_SIZE
    ): List<ActivityResponse>

    suspend fun createActivity(activity: Activity)

    suspend fun deleteActivity(activityId: String): Boolean
}