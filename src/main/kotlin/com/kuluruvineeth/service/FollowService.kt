package com.kuluruvineeth.service

import com.kuluruvineeth.data.requests.FollowUpdateRequest
import com.kuluruvineeth.repository.follow.FollowRepository

class FollowService(
    private val followRepository: FollowRepository
) {

    suspend fun followUserIfExists(request: FollowUpdateRequest): Boolean{
        return followRepository.followUserIfExists(
            request.followingUserId,
            request.followedUserId
        )
    }

    suspend fun unfollowUserIfExists(request: FollowUpdateRequest): Boolean{
        return followRepository.unfollowUserIfExists(
            request.followingUserId,
            request.followedUserId
        )
    }
}