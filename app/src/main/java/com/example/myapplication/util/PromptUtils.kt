Here is the implementation of the `PromptFactory` class. This utility isolates the prompt engineering logic, ensuring the "Genius Professor" persona is consistently applied while dynamically adapting to the user's survey data and the specific stage of the lesson.

File: app/src/main/java/com/example/myapplication/util/PromptFactory.kt
```kotlin
package com.example.myapplication.util

import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.MathRepository.LearningPhase
import java.util.Locale

/**
 * A specialized factory for generating context-aware System Prompts for the Cerebras API.
 * 
 * This class implements the "Genius Professor" persona logic. It combines:
 * 1. User Demographics (Grade, Country)
 * 2. Psychographics (Hobby, Likes, Dislikes)
 * 3. Pedagogical Structure (Summary -> Explain -> Examples -> Tasks)
 */
object PromptFactory {

    /**
     * Constructs the full System Instruction string to be sent to the LLM.
     *
     * @param userProfile The data collected from the Onboarding Survey.
     * @param phase The current stage of the learning session.
     * @param currentTopic The specific math topic currently being discussed (optional).
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
     * Defines who the AI is and the student context.
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
            - **Likes:** ${user.likes}
            - **Dislikes:** ${user.dislikes}
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
            2. **Complexity:** strictly adapt vocabulary and math concepts to the ${user.grade} level.
            3. **Engagement:** heavily use analogies related to "${user.hobby}" to explain abstract concepts. This is crucial.
            4. **Constraint:** ${avoidanceInstruction}
            5. **Formatting:** Use Markdown. Use **bold** for key terms and `code blocks` for formulas.
            6. **Context:** Since the user is from ${user.country}, use appropriate units of measurement (metric/imperial) and currency if word problems arise.
        """.trimIndent()
    }

    /**
     * 3. PHASE-SPECIFIC INSTRUCTIONS
     * Controls the structure of the lesson based on the current state.
     */
    private fun getPhaseInstructions(phase: LearningPhase, hobby: String, topic: String): String {
        return when (phase) {
            LearningPhase.SUMMARY -> """
                ### CURRENT OBJECTIVE: EXECUTIVE SUMMARY
                The student wants a quick overview of "$topic".
                
                **Instructions:**
                - Provide a high-level summary in exactly 3 bullet points.
                - Do not use complex formulas yet.
                - End with a fun "Did you know?" fact about $topic, relating it to $hobby if possible.
                - Keep the total response under 150 words.
            """.trimIndent()

            LearningPhase.EXPLANATION -> """
                ### CURRENT OBJECTIVE: DEEP DIVE EXPLANATION
                The student is ready to understand "$topic" in depth.
                
                **Instructions:**
                - Explain the concept step-by-step using the "First Principles" thinking method.
                - Define variables clearly.
                - Use a concrete analogy involving "$hobby" to explain the "Why".
                - Use the Socratic Method: ask the student a guiding question at the end to check understanding.
            """.trimIndent()

            LearningPhase.EXAMPLES -> """
                ### CURRENT OBJECTIVE: REAL-WORLD EXAMPLES
                The student needs to see "$topic" in action.
                
                **Instructions:**
                - Generate 2 distinct solved examples.
                - **Example 1:** A standard textbook problem suitable for their grade.
                - **Example 2:** A word problem specifically themed around "$hobby".
                - Show the solution steps explicitly: Given -> Formula -> Substitution -> Answer.
            """.trimIndent()

            LearningPhase.TASKS -> """
                ### CURRENT OBJECTIVE: ACTIVE PRACTICE
                It is time to test the student on "$topic".
                
                **Instructions:**
                - Generate 3 practice problems, ordered by difficulty (Easy, Medium, Hard).
                - The "Hard" problem should be a word problem related to ${hobby.lowercase(Locale.ROOT)}.
                - **CRITICAL:** Do NOT provide the solutions or answers yet.
                - Ask the student to submit their answers for verification.
            """.trimIndent()
        }
    }
}
```