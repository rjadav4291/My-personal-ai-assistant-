package com.chintu.ai.skills

import android.content.Context
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SkillManager(context: Context) {
    private val skillFile = File(context.filesDir, "chintu_skills.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    init {
        if (!skillFile.exists()) {
            saveSkills(DefaultSkills.starterSkills)
        }
    }

    @Synchronized
    fun getAvailableSkills(): List<Skill> {
        return try {
            if (!skillFile.exists()) return DefaultSkills.starterSkills
            json.decodeFromString<List<Skill>>(skillFile.readText())
        } catch (e: Exception) {
            DefaultSkills.starterSkills
        }
    }

    @Synchronized
    fun saveSkills(skills: List<Skill>) {
        skillFile.writeText(json.encodeToString(skills))
    }

    fun matchSkillForQuery(query: String): Skill? {
        val lower = query.lowercase()
        val skills = getAvailableSkills().filter { it.isEnabled }
        return when {
            lower.contains("resume") || lower.contains("સીવી") -> skills.find { it.id == "resume" }
            lower.contains("code") || lower.contains("પ્રોગ્રામ") || lower.contains("error") -> skills.find { it.id == "coding" }
            lower.contains("study") || lower.contains("ભણવા") || lower.contains("સમજાવ") -> skills.find { it.id == "study" }
            lower.contains("emi") || lower.contains("રૂપિયા") || lower.contains("વ્યાજ") || lower.contains("calculator") -> skills.find { it.id == "finance" }
            lower.contains("translate") || lower.contains("અનુવાદ") -> skills.find { it.id == "translator" }
            lower.contains("email") || lower.contains("મેઇલ") -> skills.find { it.id == "email" }
            else -> null
        }
    }
}
