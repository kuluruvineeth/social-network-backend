package com.kuluruvineeth.data.requests

import com.kuluruvineeth.data.models.Skill

data class UpdateProfileRequest(
    val username: String,
    val bio: String,
    val githubUrl: String,
    val instagramUrl: String,
    val linkedInUrl: String,
    val skills: List<Skill>
)
