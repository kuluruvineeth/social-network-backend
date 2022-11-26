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
    val userService: UserService by inject()
    val postService: PostService by inject()
    val followService: FollowService by inject()
    val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    routing {
        //User routes
        createUserRoute(userService)
        loginUser(
            userService = userService,
            jwtIssuer = jwtIssuer,
            jwtAudience = jwtAudience,
            jwtSecret = jwtSecret
        )

        //Following routes
        followUser(followService)
        unfollowUser(followService)

        //Post routes
        createPostRoute(postService,userService)
        getPostsForFollows(postService,userService)
    }
}
