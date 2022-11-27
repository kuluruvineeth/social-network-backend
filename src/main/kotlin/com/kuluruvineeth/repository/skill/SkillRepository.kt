package com.kuluruvineeth.repository.skill

import com.kuluruvineeth.data.models.Skill

interface SkillRepository {

    suspend fun getSkills(): List<Skill>
}