package com.kuluruvineeth.data.requests

data class FollowUpdateRequest(
    val followingUserId: String,
    val followedUserId: String
)
