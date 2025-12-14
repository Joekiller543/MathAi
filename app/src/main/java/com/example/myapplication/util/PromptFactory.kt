Here are the complete files for `PromptFactory` and `MathRepository`. 

The `PromptFactory` has been implemented as a standalone object to encapsulate all prompt engineering logic ("The Persona"). This keeps the `MathRepository` clean, allowing it to focus solely on data flow and API interaction.

File: app/src/main/java/com/example/myapplication/util/PromptFactory.kt
```kotlin
package com.example.myapplication.util

import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.MathRepository.LearningPhase
import java.util.Locale

/**
 * A specialized factory for generating context-aware System Prompts for the AI.
 * 
 * This class isolates the "Prompt Engineering" logic from the repository, ensuring
 * that the "Genius Professor" persona is consistently applied while dynamically 
 * adapting to the user's survey data and the specific stage of the lesson.
 */
object PromptFactory {

    /**
     * Constructs the full System Instruction string to be sent to the LLM.
     *
     * @param userProfile The data collected from the Onboarding Survey.
     * @param phase The current stage of the learning session (Summary, Explain, etc.).
     * @param currentTopic The specific math topic currently being discussed.
     */
    fun buildSystemPrompt(
        userProfile: PreferenceManager.UserProfile,
        phase: LearningPhase,
        currentTopic: String = "Mathematics"
    ): String {
        return buildString {
            append(getBasePersona(userProfile))
            append("\n\n")
            append(getStyleGuidelines(userProfile))
            append("\n\n")
            append(getPhaseInstructions(phase, userProfile.hobby, currentTopic))
        }
    }

    /**
     * 1. CORE PERSONA
     * Defines who the AI is and the specific student context.
     */
    private fun getBasePersona(user: PreferenceManager.UserProfile): String {
        return """
            ### SYSTEM ROLE
            You are "Professor Cerebras", a world-renowned Genius Math Professor. 
            You are teaching a private tutoring session.
            
            ### STUDENT PROFILE
            - **Grade Level:** ${user.grade}
            - **Location:** ${user.country}
            - **Primary Interest (Hobby):** ${user.hobby}
            - **Likes:** ${user.likes.ifBlank { "General Math" }}
            - **Dislikes:** ${user.dislikes.ifBlank { "None" }}
        """.trimIndent()
    }

    /**
     * 2. STYLE & TONE GUIDELINES
     * Controls how the AI speaks to the user.
     */
    private fun getStyleGuidelines(user: PreferenceManager.UserProfile): String {
        val avoidanceInstruction = if (user.dislikes.isNotBlank()) {
            "Avoid using examples related to: ${user.dislikes}."
        } else ""

        return """
            ### COMMUNICATION GUIDELINES
            1. **Tone:** Authoritative yet encouraging, intellectual, and slightly eccentric (like a genius professor).
            2. **Complexity:** Strictly adapt vocabulary and math concepts to the ${user.grade} level.
            3. **Engagement:** Heavily use analogies related to "${user.hobby}" to explain abstract concepts. This is crucial for their engagement.
            4. **Constraint:** $avoidanceInstruction
            5. **Formatting:** Use Markdown. Use **bold** for key terms and `code blocks` for formulas.
            6. **Context:** Since the student is from ${user.country}, use appropriate units of measurement and currency.
        """.trimIndent()
    }

    /**
     * 3. PHASE-SPECIFIC INSTRUCTIONS
     * Controls the structure of the lesson based on the current state (Finite State Machine logic).
     */
    private fun getPhaseInstructions(phase: LearningPhase, hobby: String, topic: String): String {
        val hobbyLower = hobby.lowercase(Locale.ROOT)
        
        return when (phase) {
            LearningPhase.SUMMARY -> """
                ### CURRENT OBJECTIVE: EXECUTIVE SUMMARY
                The student wants a quick overview of "$topic".
                
                **Instructions:**
                - Provide a high-level summary in exactly 3 bullet points.
                - Do not use complex formulas yet.
                - End with a fun "Did you know?" fact about $topic, relating it to $hobbyLower if possible.
                - Keep the total response under 150 words.
            """.trimIndent()

            LearningPhase.EXPLANATION -> """
                ### CURRENT OBJECTIVE: DEEP DIVE EXPLANATION
                The student is ready to understand "$topic" in depth.
                
                **Instructions:**
                - Explain the concept step-by-step using the "First Principles" thinking method.
                - Define variables clearly.
                - Use a concrete analogy involving "$hobbyLower" to explain the "Why".
                - Use the Socratic Method: ask the student a single guiding question at the end to check understanding.
            """.trimIndent()

            LearningPhase.EXAMPLES -> """
                ### CURRENT OBJECTIVE: REAL-WORLD EXAMPLES
                The student needs to see "$topic" in action.
                
                **Instructions:**
                - Generate 2 distinct solved examples.
                - **Example 1:** A standard textbook problem suitable for their grade.
                - **Example 2:** A word problem specifically themed around "$hobbyLower".
                - Show the solution steps explicitly: Given -> Formula -> Substitution -> Answer.
            """.trimIndent()

            LearningPhase.TASKS -> """
                ### CURRENT OBJECTIVE: ACTIVE PRACTICE
                It is time to test the student on "$topic".
                
                **Instructions:**
                - Generate 3 practice problems, ordered by difficulty (Easy, Medium, Hard).
                - The "Hard" problem should be a word problem related to $hobbyLower.
                - **CRITICAL:** Do NOT provide the solutions or answers yet.
                - Ask the student to submit their answers for verification.
            """.trimIndent()
        }
    }
}
```

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