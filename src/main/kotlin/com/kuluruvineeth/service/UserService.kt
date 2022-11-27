package com.kuluruvineeth.service

import com.kuluruvineeth.data.models.User
import com.kuluruvineeth.data.requests.CreateAccountRequest
import com.kuluruvineeth.data.requests.LoginRequest
import com.kuluruvineeth.data.requests.UpdateProfileRequest
import com.kuluruvineeth.data.responses.ProfileResponse
import com.kuluruvineeth.data.responses.UserResponseItem
import com.kuluruvineeth.repository.follow.FollowRepository
import com.kuluruvineeth.repository.user.UserRepository

class UserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository
){

    suspend fun doesUserWithEmailExist(email:String): Boolean{
        return userRepository.getUserByEmail(email) != null
    }

    suspend fun doesEmailBelongToUserId(email: String,userId:String): Boolean{
        return userRepository.doesEmailBelongToUserId(email,userId)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userRepository.getUserByEmail(email)
    }

    fun isValidPassword(enteredPassword: String, actualPassword: String): Boolean{
        return enteredPassword == actualPassword
    }

    suspend fun doesPasswordMatchForUser(request: LoginRequest): Boolean {
        return userRepository.doesPasswordForUserMatch(
            email = request.email,
            enteredPassword = request.password
        )
    }

    suspend fun createUser(request: CreateAccountRequest){
        userRepository.createUser(
            User(
                email = request.email,
                username = request.username,
                password = request.password,
                profileImageUrl = "",
                bannerUrl = "",
                bio = "",
                githubUrl = null,
                instagramUrl = null,
                linkedInUrl = null
            )
        )
    }

    suspend fun searchForUsers(query: String, userId: String): List<UserResponseItem>{
        val users = userRepository.searchForUsers(query)
        val followsByUser = followRepository.getFollowsByUser(userId)
        return users.map { user ->
            val isFollowing = followsByUser.find { it.followedUserId == userId } != null
            UserResponseItem(
                userId = user.id,
                username = user.username,
                profilePictureUrl = user.profileImageUrl,
                bio = user.bio,
                isFollowing = isFollowing
            )
        }
    }

    suspend fun getUserProfile(userId: String,callerUserId:String): ProfileResponse? {
        val user = userRepository.getUserById(userId) ?: return null
        return ProfileResponse(
            userId = user.id,
            username = user.username,
            bio = user.bio,
            followerCount = user.followerCount,
            followingCount = user.followingCount,
            postCount = user.postCount,
            profilePictureUrl = user.profileImageUrl,
            bannerUrl = user.bannerUrl,
            topSkills = user.skills.map{it.toSkillResponse()},
            githubUrl = user.githubUrl,
            instagramUrl = user.instagramUrl,
            linkedInUrl = user.linkedInUrl,
            isOwnProfile = userId == callerUserId,
            isFollowing = if(userId != callerUserId){
                followRepository.doesUserFollow(callerUserId,userId)
            }else{
                false
            }
        )
    }

    suspend fun updateUser(
        userId: String,
        profileImageUrl: String?,
        bannerUrl: String?,
        updateProfileRequest: UpdateProfileRequest
    ): Boolean{
        return userRepository.updateUser(userId,profileImageUrl,bannerUrl,updateProfileRequest)
    }

    fun validateCreateAccountRequest(request: CreateAccountRequest) : ValidationEvent{
        if(request.email.isBlank() || request.password.isBlank() || request.username.isBlank()){
            return ValidationEvent.ErrorFieldEmpty
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent{
        object ErrorFieldEmpty : ValidationEvent()
        object Success : ValidationEvent()
    }
}