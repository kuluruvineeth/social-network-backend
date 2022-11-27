package com.kuluruvineeth.routes

import com.google.gson.Gson
import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.requests.UpdateProfileRequest
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.data.responses.UserResponseItem
import com.kuluruvineeth.service.PostService
import com.kuluruvineeth.service.UserService
import com.kuluruvineeth.util.ApiResponseMessages
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.Constants.BASE_URL
import com.kuluruvineeth.util.QueryParams
import com.kuluruvineeth.util.save
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

fun Route.searchUser(userService: UserService){
    authenticate {
        get("/api/user/search"){
            val query = call.parameters[QueryParams.PARAM_QUERY]
            if(query==null || query.isBlank()){
                call.respond(
                    HttpStatusCode.OK,
                    listOf<UserResponseItem>()
                )
                return@get
            }
            val searchResults = userService.searchForUsers(query,call.userId)
            call.respond(
                HttpStatusCode.OK,
                searchResults
            )
        }
    }
}

fun Route.getPostsForProfile(
    postService: PostService
){
    authenticate {
        get("/api/user/posts"){
            val userId = call.parameters[QueryParams.PARAM_USER_ID]
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_POST_PAGE_SIZE

            val posts = postService.getPostsForProfile(
                userId = userId ?: call.userId,
                page = page,
                pageSize = pageSize
            )
            call.respond(
                HttpStatusCode.OK,
                posts
            )
        }
    }
}

fun Route.getUserProfile(userService: UserService){
    authenticate {
        get("/api/user/profile"){
            val userId = call.parameters[QueryParams.PARAM_USER_ID]
            if(userId == null || userId.isBlank()){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val profileResponse = userService.getUserProfile(userId,call.userId)
            if(profileResponse == null){
                call.respond(
                    HttpStatusCode.OK, BasicApiResponse<Unit>(
                        successful = false,
                        message = ApiResponseMessages.USER_NOT_FOUND
                    )
                )
                return@get
            }
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = true,
                    data = profileResponse.also {
                        println("RESPONDING WITH $it")
                    }
                )
            )
        }
    }
}


fun Route.updateUserProfile(userService: UserService){
    val gson: Gson by inject()
    authenticate {
        put("/api/user/update"){
            val multipart = call.receiveMultipart()
            var updateProfileRequest: UpdateProfileRequest? = null
            var profilePictureFileName: String? = null
            var bannerImageFileName: String? = null
            multipart.forEachPart { partData ->
                when(partData){
                    is PartData.FormItem -> {
                        if(partData.name == "update_profile_data"){
                            updateProfileRequest = gson.fromJson(
                                partData.value,
                                UpdateProfileRequest::class.java
                            )
                        }
                    }
                    is PartData.FileItem -> {
                        if(partData.name == "profile_picture"){
                            profilePictureFileName = partData.save(Constants.PROFILE_PICTURE_PATH)
                        }else if(partData.name == "banner_image"){
                            bannerImageFileName = partData.save(Constants.BANNER_IMAGE_PATH)
                        }
                    }
                    is PartData.BinaryItem -> Unit
                    is PartData.BinaryChannelItem -> Unit
                }
            }

            val profilePictureUrl = "${Constants.BASE_URL}profile_pictures/$profilePictureFileName"
            val bannerImageUrl = "${BASE_URL}banner_images/$bannerImageFileName"

            updateProfileRequest?.let { request ->
                val updateAcknowledged = userService.updateUser(
                    userId = call.userId,
                    profileImageUrl = if(profilePictureFileName == null){
                        null
                    }else{
                        profilePictureUrl
                         },
                    bannerUrl = if(bannerImageFileName == null){
                        null
                    }else{
                        bannerImageUrl
                    },
                    updateProfileRequest = request
                )
                if(updateAcknowledged){
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = true
                        )
                    )
                }else{
                    File("${Constants.PROFILE_PICTURE_PATH}/$profilePictureFileName").delete()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }
    }
}