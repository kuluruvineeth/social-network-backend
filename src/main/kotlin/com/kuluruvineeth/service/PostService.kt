package com.kuluruvineeth.service

import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.requests.CreatePostRequest
import com.kuluruvineeth.repository.post.PostRepository

class PostService(
    private val repository: PostRepository
) {

    suspend fun createPostIfUserExists(request: CreatePostRequest): Boolean{
        return repository.createPostIfUserExists(
            Post(
                imageUrl = "",
                userId = request.userId,
                timestamp = System.currentTimeMillis(),
                description = request.description
            )
        )
    }
}