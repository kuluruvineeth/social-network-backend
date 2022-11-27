package com.kuluruvineeth.service

import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.requests.CreatePostRequest
import com.kuluruvineeth.data.responses.PostResponse
import com.kuluruvineeth.repository.post.PostRepository
import com.kuluruvineeth.util.Constants

class PostService(
    private val repository: PostRepository
) {

    suspend fun createPost(request: CreatePostRequest,userId: String,imageUrl:String): Boolean{
        return repository.createPost(
            Post(
                imageUrl = imageUrl,
                userId = userId,
                timestamp = System.currentTimeMillis(),
                description = request.description
            )
        )
    }

    suspend fun getPostsForFollows(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_POST_PAGE_SIZE
    ):List<Post>{
        return repository.getPostsByFollows(
            userId,
            page,
            pageSize
        )
    }

    suspend fun getPostsForProfile(
        ownUserId: String,
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_POST_PAGE_SIZE
    ): List<PostResponse>{
        return repository.getPostsForProfile(ownUserId,userId,page,pageSize)
    }

    suspend fun getPost(postId: String): Post? = repository.getPost(postId)

    suspend fun deletePost(postId: String){
        repository.deletePost(postId)
    }

    suspend fun getPostDetails(ownUserId: String, postId: String): PostResponse?{
        return repository.getPostDetails(ownUserId,postId)
    }
}