File: app/src/main/java/com/example/myapplication/data/repository/MathRepository.kt
```kotlin
package com.example.myapplication.data.repository

import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.util.PromptFactory

/**
 * Repository responsible for the Core AI Logic.
 * 
 * It acts as the mediator between the UI, the User Preferences, and the Network Layer.
 * It uses [PromptFactory] to construct the prompt and [CerebrasRepository] to execute it.
 */
class MathRepository(
    private val cerebrasRepository: CerebrasRepository,
    private val preferenceManager: PreferenceManager
) {

    /**
     * Represents the specific stage of the learning process.
     * This State determines the structure of the AI's response.
     */
    enum class LearningPhase {
        SUMMARY,
        EXPLANATION,
        EXAMPLES,
        TASKS
    }

    /**
     * Generates a tailored response for the student.
     * 
     * @param query The user's math question or input.
     * @param phase The current stage of the conversation flow.
     * @return A Result containing the AI's text response.
     */
    suspend fun getAssistantResponse(
        query: String,
        phase: LearningPhase
    ): Result<String> {
        // 1. Retrieve User Context (Personalization)
        // This data comes from the persisted survey (Grade, Hobby, Country, etc.)
        val userProfile = preferenceManager.getUserData()

        // 2. Construct the System Prompt
        // We delegate the prompt building to the Factory to keep the repository clean.
        // The 'query' acts as the topic context for the instructions.
        val systemInstruction = PromptFactory.buildSystemPrompt(
            userProfile = userProfile,
            phase = phase,
            currentTopic = query
        )

        // 3. Combine Instruction and Query
        // Since the API is stateless, we prepend the System Persona to the user's actual question.
        val finalPrompt = """
            $systemInstruction
            
            [STUDENT QUESTION]:
            "$query"
        """.trimIndent()

        // 4. Delegate to Network Layer
        return cerebrasRepository.sendPrompt(finalPrompt)
    }
}
```