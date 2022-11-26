package com.kuluruvineeth.routes

import com.kuluruvineeth.data.requests.CreateCommentRequest
import com.kuluruvineeth.data.requests.DeleteCommentRequest
import com.kuluruvineeth.data.requests.DeletePostRequest
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.service.ActivityService
import com.kuluruvineeth.service.CommentService
import com.kuluruvineeth.service.LikeService
import com.kuluruvineeth.service.UserService
import com.kuluruvineeth.util.ApiResponseMessages.COMMENT_TOO_LONG
import com.kuluruvineeth.util.ApiResponseMessages.FIELDS_BLANK
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createComment(
    commentService: CommentService,
    activityService: ActivityService
){
    authenticate {
        post("/api/comment/create"){
            val request = kotlin.runCatching { call.receiveNullable<CreateCommentRequest>() }?.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val userId = call.userId
            when(commentService.createComment(request,userId)){
                is CommentService.ValidationEvent.ErrorFieldEmpty -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = false,
                            message = FIELDS_BLANK
                        )
                    )
                }
                is CommentService.ValidationEvent.ErrorCommentTooLong -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = false,
                            message = COMMENT_TOO_LONG
                        )
                    )
                }
                is CommentService.ValidationEvent.Success -> {
                    activityService.addCommentActivity(
                        byUserId = userId,
                        postId = request.postId
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = true
                        )
                    )
                }
            }
        }
    }
}


fun Route.getCommentsForPost(
    commentService: CommentService,
){
    authenticate {
        get("/api/comment/get") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run{
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val comments = commentService.getCommentsForPost(postId)
            call.respond(HttpStatusCode.OK,comments)
        }
    }
}

fun Route.deleteComment(
    commentService: CommentService,
    likeService: LikeService
){
    authenticate {
        delete("/api/comment/delete"){
            val request = kotlin.runCatching { call.receiveNullable<DeleteCommentRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val comment = commentService.getCommentById(request.commentId)
            if(comment?.userId != call.userId){
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }
            val deleted = commentService.deleteComment(request.commentId)
            if(deleted){
                likeService.deleteLikesForParent(request.commentId)
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        successful = true
                    )
                )
            }else{
                call.respond(HttpStatusCode.NotFound,BasicApiResponse(successful = false))
            }
        }
    }
}