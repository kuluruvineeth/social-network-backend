package com.kuluruvineeth.routes


import com.kuluruvineeth.data.models.Activity
import com.kuluruvineeth.data.requests.FollowUpdateRequest
import com.kuluruvineeth.data.requests.LoginRequest
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.data.util.ActivityType
import com.kuluruvineeth.repository.follow.FollowRepository
import com.kuluruvineeth.service.ActivityService
import com.kuluruvineeth.service.FollowService
import com.kuluruvineeth.util.ApiResponseMessages.USER_NOT_FOUND
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.followUser(
    followService: FollowService,
    activityService: ActivityService
){
    authenticate {
        post("/api/following/follow"){
            val request = kotlin.runCatching { call.receiveNullable<FollowUpdateRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val didUserExist = followService.followUserIfExists(request,call.userId)
            if(didUserExist){
                activityService.createActivity(
                    Activity(
                        timestamp = System.currentTimeMillis(),
                        byUserId = call.userId,
                        toUserId = request.followedUserId,
                        type = ActivityType.FollowedUser.type,
                        parentId = ""
                    )
                )
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        successful = true
                    )
                )
            }else{
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        successful = false,
                        message = USER_NOT_FOUND
                    )
                )
            }
        }
    }

}

fun Route.unfollowUser(followService: FollowService){

    authenticate {
        delete("/api/following/unfollow"){
            val userId = call.parameters[QueryParams.PARAM_USER_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val didUserExist = followService.unfollowUserIfExists(userId,call.userId)
            if(didUserExist){
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        successful = true
                    )
                )
            }else{
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        successful = false,
                        message = USER_NOT_FOUND
                    )
                )
            }
        }
    }

}