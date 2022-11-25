package com.kuluruvineeth.plugins

import com.kuluruvineeth.repository.follow.FollowRepository
import com.kuluruvineeth.repository.post.PostRepository
import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.routes.*
import com.kuluruvineeth.service.FollowService
import com.kuluruvineeth.service.PostService
import com.kuluruvineeth.service.UserService
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository: UserRepository by inject()
    val userService: UserService by inject()
    val postService: PostService by inject()
    val followService: FollowService by inject()
    val followRepository: FollowRepository by inject()
    val postRepository: PostRepository by inject()
    routing {
        //User routes
        createUserRoute(userService)
        loginUser(userRepository)

        //Following routes
        followUser(followService)
        unfollowUser(followService)

        //Post routes
        createPostRoute(postService)
    }
}
