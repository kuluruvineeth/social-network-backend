package com.kuluruvineeth.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic
import com.google.gson.Gson
import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.requests.CreateAccountRequest
import com.kuluruvineeth.data.requests.LoginRequest
import com.kuluruvineeth.data.requests.UpdateProfileRequest
import com.kuluruvineeth.data.responses.AuthResponse
import com.kuluruvineeth.data.responses.BasicApiResponse
import com.kuluruvineeth.service.PostService
import com.kuluruvineeth.service.UserService
import com.kuluruvineeth.util.ApiResponseMessages
import com.kuluruvineeth.util.ApiResponseMessages.FIELDS_BLANK
import com.kuluruvineeth.util.ApiResponseMessages.INVALID_CREDENTIALS
import com.kuluruvineeth.util.ApiResponseMessages.USER_NOT_FOUND
import com.kuluruvineeth.util.Constants
import com.kuluruvineeth.util.Constants.BASE_URL
import com.kuluruvineeth.util.Constants.PROFILE_PICTURE_PATH
import com.kuluruvineeth.util.QueryParams
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File
import java.util.*

fun Route.createUser(userService: UserService){
    route("/api/user/create"){
        post {
            val request = kotlin.runCatching { call.receiveNullable<CreateAccountRequest>() }.getOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if(userService.doesUserWithEmailExist(request.email)){
                call.respond(
                    BasicApiResponse(
                        successful = false,
                        message = ApiResponseMessages.USER_ALREADY_EXISTS
                    )
                )
                return@post
            }
            when(userService.validateCreateAccountRequest(request)){
                is UserService.ValidationEvent.ErrorFieldEmpty -> {
                    call.respond(
                        BasicApiResponse(
                            successful = false,
                            message = FIELDS_BLANK
                        )
                    )
                }
                is UserService.ValidationEvent.Success -> {
                    userService.createUser(request)
                    call.respond(
                        BasicApiResponse(successful = true)
                    )
                }
            }

        }
    }
}


fun Route.loginUser(
    userService: UserService,
    jwtIssuer: String,
    jwtAudience: String,
    jwtSecret: String
){
    post("/api/user/login"){
        val request = kotlin.runCatching { call.receiveNullable<LoginRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if(request.email.isBlank() || request.password.isBlank()){
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userService.getUserByEmail(request.email) ?: kotlin.run {
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = false,
                    message = INVALID_CREDENTIALS
                )
            )
            return@post
        }
        val isCorrectPassword = userService.isValidPassword(
            enteredPassword = request.password,
            actualPassword = user.password
        )

        if(isCorrectPassword){
            val expiresIn = 1000L * 60L * 60L * 24L * 365L
            val token = JWT.create()
                .withClaim("userId",user.id)
                .withIssuer(jwtIssuer)
                .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
                .withAudience(jwtAudience)
                .sign(Algorithm.HMAC256(jwtSecret))
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    token = token
                )
            )
        }else{
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = false,
                    message = ApiResponseMessages.INVALID_CREDENTIALS
                )
            )
        }

    }
}

fun Route.searchUser(userService: UserService){
    authenticate {
        get("/api/user/search"){
            val query = call.parameters[QueryParams.PARAM_QUERY]
            if(query==null || query.isBlank()){
                call.respond(
                    HttpStatusCode.OK,
                    listOf<User>()
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
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize = call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_POST_PAGE_SIZE

            val posts = postService.getPostsForProfile(
                userId = call.userId,
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
                    HttpStatusCode.OK, BasicApiResponse(
                        successful = false,
                        message = USER_NOT_FOUND
                    )
                )
                return@get
            }
            call.respond(
                HttpStatusCode.OK,
                profileResponse
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
            var fileName: String? = null
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
                        val fileBytes = partData.streamProvider().readBytes()
                        val fileExtension = partData.originalFileName?.takeLastWhile { it != '.' }
                        fileName = UUID.randomUUID().toString() + "." + fileExtension
                        File("$PROFILE_PICTURE_PATH$fileName").writeBytes(fileBytes)
                    }
                    is PartData.BinaryItem -> Unit
                    is PartData.BinaryChannelItem -> Unit
                }
            }

            val profilePictureUrl = "${BASE_URL}profile_pictures/$fileName"

            updateProfileRequest?.let { request ->
                val updateAcknowledged = userService.updateUser(
                    userId = call.userId,
                    profileImageUrl = profilePictureUrl,
                    updateProfileRequest = request
                )
                if(updateAcknowledged){
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            successful = true
                        )
                    )
                }else{
                    File("${PROFILE_PICTURE_PATH}/$fileName").delete()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }
    }
}