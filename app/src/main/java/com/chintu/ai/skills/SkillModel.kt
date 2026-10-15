package com.chintu.ai.skills

import kotlinx.serialization.Serializable

@Serializable
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val systemPromptModifier: String,
    val isEnabled: Boolean = true
)

object DefaultSkills {
    val starterSkills = listOf(
        Skill("resume", "Resume Creator", "Creates professional ATS-friendly resumes", "You are an expert HR and resume strategist. Formulate structured, ATS-compliant bullet points with metrics."),
        Skill("coding", "Coding Assistant", "Writes, debugs, and refactors Kotlin, Python, JS code", "You are a senior staff software engineer. Provide robust, clean, idiomatic code with clear error handling."),
        Skill("study", "Study Assistant", "Explains complex topics and generates study notes", "You are an encouraging academic tutor. Break down complex topics simply, using analogies and end with 3 review questions."),
        Skill("finance", "Finance & EMI Calculator", "Helps calculate budgets, savings, and investments", "You are a financial advisor assistant. Break down numbers, formulas, interest, and practical risk assessments."),
        Skill("translator", "Language Translator", "Translates between Gujarati, Hindi, and English", "You are a professional linguist. Provide culturally accurate, natural translations between Gujarati, Hindi, and English."),
        Skill("email", "Email Drafter", "Drafts professional, formal, and casual emails", "You are an executive assistant. Draft concise, high-impact emails with clear subject lines and call-to-actions.")
    )
}
