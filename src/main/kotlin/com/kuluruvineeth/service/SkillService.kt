package com.kuluruvineeth.service

import com.kuluruvineeth.data.models.Skill
import com.kuluruvineeth.repository.skill.SkillRepository

class SkillService(
    private val repository: SkillRepository
) {

    suspend fun getSkills(): List<Skill>{
        return repository.getSkills()
    }
}