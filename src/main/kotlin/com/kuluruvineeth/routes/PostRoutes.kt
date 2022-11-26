package com.kuluruvineeth.routes

import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.requests.CreatePostRequest
import com.kuluruvineeth.data.requests.FollowUpdateRequest
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.plugins.email
import com.kuluruvineeth.repository.post.PostRepository
import com.kuluruvineeth.service.PostService
import com.kuluruvineeth.service.UserService
import com.kuluruvineeth.util.ApiResponseMessages
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createPostRoute(
    postService: PostService,
    userService: UserService
){
    authenticate {
        post("/api/post/create"){
            val request = kotlin.runCatching { call.receiveNullable<CreatePostRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            ifEmailBelongsToUser(
                userId = request.userId,
                validateEmail = userService::doesEmailBelongToUserId
            ){
                val didUserExist = postService.createPostIfUserExists(request)
                if(!didUserExist){
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = false,
                            message = ApiResponseMessages.USER_NOT_FOUND
                        )
                    )
                }else{
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = true,
                        )
                    )
                }
            }


        }
    }
}


fun Route.getPostsForFollows(
    postService: PostService,
    userService: UserService
){
    authenticate {
        get{
            val userId = call.parameters[QueryParams.PARAM_USER_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_POST_PAGE_SIZE

            ifEmailBelongsToUser(
                userId = userId,
                validateEmail = userService::doesEmailBelongToUserId
            ){
                val posts = postService.getPostsForFollows(userId,page,pageSize)
                call.respond(
                    HttpStatusCode.OK,
                    posts
                )
            }
        }
    }
}