Here is the updated `LearningViewModel.kt` file. It is configured to accept `PreferenceManager` via the constructor (to support the personalization features) and has been cleaned of any markdown artifacts.

```kotlin
package com.example.myapplication.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.MathRepository
import com.example.myapplication.data.repository.MathRepository.LearningPhase
import com.example.myapplication.util.SimpleCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI State holding the chat history, current learning phase, user context, and calculator status.
 */
data class LearningUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val currentPhase: LearningPhase = LearningPhase.SUMMARY,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userHobby: String = "",
    
    // Calculator Tool State
    val isCalculatorVisible: Boolean = false,
    val calcExpression: String = "",
    val calcResult: String = ""
)

/**
 * UI model representing a single chat message in the conversation.
 */
data class ChatMessageUi(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ViewModel acting as the "Thinking System" for the learning session.
 * 
 * Responsibilities:
 * 1. Coordinates between the UI and MathRepository.
 * 2. Manages the Chat History and UI State.
 * 3. Injects PreferenceManager to personalize the session (Persona adaptation).
 * 4. Handles utility tools like the Calculator.
 */
class LearningViewModel(
    private val mathRepository: MathRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        initializeSession()
    }

    /**
     * Bootstraps the session by loading user preferences and generating a persona-based welcome.
     */
    private fun initializeSession() {
        val userProfile = preferenceManager.getUserData()
        
        // Update state with user context (can be used for UI hints)
        _uiState.value = _uiState.value.copy(userHobby = userProfile.hobby)

        // Initial "Professor" greeting based on survey data
        val welcomeMessage = """
            Hello! I am your AI Math Professor.
            
            I see you enjoy ${userProfile.hobby}. That's excellent! We can use that to visualize abstract concepts.
            
            What math topic are we tackling today? We can start with a Summary.
        """.trimIndent()

        addMessage(welcomeMessage, isUser = false)
    }

    /**
     * Handles the selection of a new Learning Phase (Summary -> Explanation -> Examples -> Quiz).
     */
    fun onPhaseSelected(phase: LearningPhase) {
        _uiState.value = _uiState.value.copy(currentPhase = phase)
        
        // We do not automatically trigger a request here; we wait for the user to ask 
        // a question in this new context, or we could auto-prompt:
        // sendMessage("Please give me a ${phase.name.lowercase()} of this topic.")
    }

    /**
     * Processes user input, updates history, and fetches AI response.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentState = _uiState.value
        
        // 1. Display User Message immediately
        addMessage(text, isUser = true)
        
        // 2. Set Loading State
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        // 3. Delegate "Thinking" to Repository
        viewModelScope.launch {
            val result = mathRepository.getAssistantResponse(
                query = text,
                phase = currentState.currentPhase
            )

            result.onSuccess { responseText ->
                addMessage(responseText, isUser = false)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Brain freeze! Check your connection. (${e.localizedMessage})"
                )
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        val newMessage = ChatMessageUi(text = text, isUser = isUser)
        val currentList = _uiState.value.messages.toMutableList()
        currentList.add(newMessage)
        _uiState.value = _uiState.value.copy(messages = currentList)
    }

    // region Calculator Tool Logic

    fun toggleCalculator() {
        _uiState.value = _uiState.value.copy(isCalculatorVisible = !_uiState.value.isCalculatorVisible)
    }

    fun onCalculatorInput(input: String) {
        var expr = _uiState.value.calcExpression
        
        when (input) {
            "C" -> {
                expr = ""
                _uiState.value = _uiState.value.copy(calcResult = "")
            }
            "DEL" -> {
                if (expr.isNotEmpty()) expr = expr.dropLast(1)
            }
            "=" -> {
                val result = SimpleCalculator.evaluate(expr)
                _uiState.value = _uiState.value.copy(calcResult = result)
                return // Return early to keep expression visible
            }
            "sin", "cos", "tan" -> {
                expr += "$input("
            }
            else -> {
                expr += input
            }
        }
        _uiState.value = _uiState.value.copy(calcExpression = expr)
    }

    // endregion
}
```