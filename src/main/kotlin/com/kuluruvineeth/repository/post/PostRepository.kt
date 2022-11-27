package com.kuluruvineeth.repository.post

import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.responses.PostResponse
import com.kuluruvineeth.util.Constants

interface PostRepository {

    suspend fun createPost(post: Post) : Boolean

    suspend fun deletePost(postId: String)

    suspend fun getPostsByFollows(
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_POST_PAGE_SIZE
    ): List<PostResponse>

    suspend fun getPostsForProfile(
        ownUserId: String,
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_POST_PAGE_SIZE
    ): List<PostResponse>

    suspend fun getPost(postId: String): Post?

    suspend fun getPostDetails(userId: String,postId: String): PostResponse?
}