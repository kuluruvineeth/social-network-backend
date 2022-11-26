package com.kuluruvineeth.repository.post

import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.util.Constants

interface PostRepository {

    suspend fun createPostIfUserExists(post: Post) : Boolean

    suspend fun deletePost(postId: String)

    suspend fun getPostsByFollows(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_POST_PAGE_SIZE
    ): List<Post>
}