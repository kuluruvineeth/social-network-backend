package com.kuluruvineeth.repository.post

import com.kuluruvineeth.data.models.Following
import com.kuluruvineeth.data.models.Like
import com.kuluruvineeth.data.models.Post
import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.responses.PostResponse
import com.kuluruvineeth.util.Constants
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq
import org.litote.kmongo.`in`

class PostRepositoryImpl(
    db: CoroutineDatabase
) : PostRepository {

    private val posts = db.getCollection<Post>()
    private val following = db.getCollection<Following>()
    private val users = db.getCollection<User>()
    private val likes = db.getCollection<Like>()

    override suspend fun createPost(post: Post): Boolean {
        return posts.insertOne(post).wasAcknowledged()
    }

    override suspend fun deletePost(postId: String) {
        posts.deleteOneById(postId)
    }

    override suspend fun getPostsByFollows(
        userId: String,
        page: Int,
        pageSize: Int
    ): List<Post> {
        val userIdsFromFollows = following.find(
            Following::followingUserId eq userId
        ).toList()
            .map {
                it.followingUserId
            }
        return posts.find(Post::userId `in` userIdsFromFollows)
            .skip(page * pageSize)
            .limit(pageSize)
            .descendingSort(Post::timestamp)
            .toList()

    }

    override suspend fun getPostsForProfile(userId: String, page: Int, pageSize: Int): List<Post> {
        return posts.find(Post::userId eq userId)
            .skip(page * pageSize)
            .limit(pageSize)
            .descendingSort(Post::timestamp)
            .toList()
    }

    override suspend fun getPost(postId: String): Post? {
        return posts.findOneById(postId)
    }

    override suspend fun getPostDetails(userId: String,postId: String): PostResponse? {
        val isLiked = likes.findOne(Like::userId eq userId) != null
        val post = posts.findOneById(postId) ?: return null
        val user = users.findOneById(userId) ?: return null
        return PostResponse(
            id = post.id,
            userId = user.id,
            username = user.username,
            imageUrl = post.imageUrl,
            profilePictureUrl = user.profileImageUrl,
            description = post.description,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            isLiked = isLiked
        )
    }
}