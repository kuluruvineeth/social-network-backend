package com.kuluruvineeth.repository.activity

import com.kuluruvineeth.data.models.Activity
import com.kuluruvineeth.data.models.Following
import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.responses.ActivityResponse
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.`in`

class ActivityRepositoryImpl(
    db: CoroutineDatabase
) : ActivityRepository{

    private val users = db.getCollection<User>()
    private val activities = db.getCollection<Activity>()

    override suspend fun getActivitiesForUser(
        userId: String,
        page: Int,
        pageSize: Int
    ): List<ActivityResponse> {
        val activities =  activities.find(Activity::toUserId eq userId)
            .skip(page * pageSize)
            .limit(pageSize)
            .descendingSort(Activity::timestamp)
            .toList()
        val userIds = activities.map { it.byUserId }
        val users = users.find(User::id `in` userIds).toList()
        return activities.mapIndexed { i, activity ->
            ActivityResponse(
                timestamp = activity.timestamp,
                userId = activity.byUserId,
                parentId = activity.parentId,
                type = activity.type,
                username = users[i].username,
                id = activity.id
            )
        }

    }

    override suspend fun createActivity(activity: Activity){
        activities.insertOne(activity)
    }

    override suspend fun deleteActivity(activityId: String): Boolean {
        return activities.deleteOneById(activityId).wasAcknowledged()
    }
}