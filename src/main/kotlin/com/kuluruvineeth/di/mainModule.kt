package com.kuluruvineeth.di

import com.google.gson.Gson
import com.kuluruvineeth.repository.activity.ActivityRepository
import com.kuluruvineeth.repository.activity.ActivityRepositoryImpl
import com.kuluruvineeth.repository.chat.ChatRepository
import com.kuluruvineeth.repository.chat.ChatRepositoryImpl
import com.kuluruvineeth.repository.comment.CommentRepository
import com.kuluruvineeth.repository.comment.CommentRepositoryImpl
import com.kuluruvineeth.repository.follow.FollowRepository
import com.kuluruvineeth.repository.follow.FollowRepositoryImpl
import com.kuluruvineeth.repository.likes.LikeRepository
import com.kuluruvineeth.repository.likes.LikeRepositoryImpl
import com.kuluruvineeth.repository.post.PostRepository
import com.kuluruvineeth.repository.post.PostRepositoryImpl
import com.kuluruvineeth.repository.skill.SkillRepository
import com.kuluruvineeth.repository.skill.SkillRepositoryImpl
import com.kuluruvineeth.repository.user.UserRepository
import com.kuluruvineeth.repository.user.UserRepositoryImpl
import com.kuluruvineeth.service.*
import com.kuluruvineeth.service.chat.ChatController
import com.kuluruvineeth.service.chat.ChatService
import com.kuluruvineeth.util.Constants
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineClient
import com.mongodb.reactivestreams.client.MongoClient
import org.litote.kmongo.reactivestreams.*  // KMongo reactivestreams extensions

private val MongoClient.coroutine: CoroutineClient get() = CoroutineClient(this)



val mainModule = module {
    single {
        val client = KMongo.createClient().coroutine
        client.getDatabase(Constants.DATABASE_NAME)
    }
    single<UserRepository>{
        UserRepositoryImpl(get())
    }
    single<FollowRepository> {
        FollowRepositoryImpl(get())
    }
    single<PostRepository>{
        PostRepositoryImpl(get())
    }
    single<LikeRepository>{
        LikeRepositoryImpl(get())
    }
    single<CommentRepository>{
        CommentRepositoryImpl(get())
    }
    single<ActivityRepository>{
        ActivityRepositoryImpl(get())
    }
    single<SkillRepository>{
        SkillRepositoryImpl(get())
    }
    single<ChatRepository>{
        ChatRepositoryImpl(get())
    }
    single { UserService(get(),get()) }
    single { PostService(get()) }
    single { FollowService(get()) }
    single { LikeService(get(),get(),get()) }
    single { CommentService(get(),get()) }
    single { ActivityService(get(),get(),get()) }
    single { SkillService(get()) }
    single { ChatService(get()) }
    single { Gson() }

    single { ChatController(get()) }
}