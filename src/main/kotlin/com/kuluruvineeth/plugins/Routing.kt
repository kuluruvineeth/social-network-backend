package com.kuluruvineeth.plugins

import com.kuluruvineeth.repository.follow.FollowRepository
import com.kuluruvineeth.repository.post.PostRepository
import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.routes.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository: UserRepository by inject()
    val followRepository: FollowRepository by inject()
    val postRepository: PostRepository by inject()
    routing {
        //User routes
        createUserRoute(userRepository)
        loginUser(userRepository)

        //Following routes
        followUser(followRepository)
        unfollowUser(followRepository)

        //Post routes
        createPostRoute(postRepository)
    }
}
