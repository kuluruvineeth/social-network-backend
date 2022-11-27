package com.kuluruvineeth.service

import com.kuluruvineeth.data.requests.FollowUpdateRequest
import com.kuluruvineeth.repository.follow.FollowRepository

class FollowService(
    private val followRepository: FollowRepository
) {

    suspend fun followUserIfExists(request: FollowUpdateRequest,followingUserId:String): Boolean{
        return followRepository.followUserIfExists(
            followingUserId,
            request.followedUserId
        )
    }

    suspend fun unfollowUserIfExists(followedUserId: String,followingUserId:String): Boolean{
        return followRepository.unfollowUserIfExists(
            followingUserId,
            followedUserId
        )
    }
}