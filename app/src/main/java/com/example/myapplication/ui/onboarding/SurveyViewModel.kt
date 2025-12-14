File: app/src/main/java/com/example/myapplication/ui/onboarding/SurveyViewModel.kt
```kotlin
package com.example.myapplication.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.util.AppFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Onboarding Survey screen.
 */
data class SurveyUiState(
    val hobby: String = "",
    val grade: String = "",
    val country: String = "",
    val likes: String = "",
    val dislikes: String = "",
    val isComplete: Boolean = false,
    val isLoading: Boolean = false,
    val folderCreated: Boolean = false,
    val permissionRationaleNeeded: Boolean = false
)

/**
 * ViewModel for managing the Onboarding Survey logic.
 * 
 * Responsibilities:
 * 1. Manages form state (Hobby, Grade, etc.).
 * 2. Handles storage permission results and folder creation via [AppFileManager].
 * 3. Persists user profile data via [PreferenceManager].
 * 4. Determines if the onboarding flow is complete to direct navigation.
 */
class SurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    
    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    init {
        checkStartupStatus()
    }

    /**
     * Checks if the user has already completed the survey previously.
     */
    private fun checkStartupStatus() {
        val isDone = preferenceManager.isSetupDone()
        // If done, we load existing data into the fields (optional) or just mark complete
        if (isDone) {
            val userData = preferenceManager.getUserData()
            _uiState.value = _uiState.value.copy(
                isComplete = true,
                hobby = userData.hobby,
                grade = userData.grade,
                country = userData.country,
                likes = userData.likes,
                dislikes = userData.dislikes
            )
        }
    }

    // region Input Handlers

    fun onHobbyChange(newValue: String) {
        _uiState.value = _uiState.value.copy(hobby = newValue)
    }

    fun onGradeChange(newValue: String) {
        _uiState.value = _uiState.value.copy(grade = newValue)
    }

    fun onCountryChange(newValue: String) {
        _uiState.value = _uiState.value.copy(country = newValue)
    }

    fun onLikesChange(newValue: String) {
        _uiState.value = _uiState.value.copy(likes = newValue)
    }

    fun onDislikesChange(newValue: String) {
        _uiState.value = _uiState.value.copy(dislikes = newValue)
    }

    // endregion

    // region Permission & Storage Logic

    /**
     * Called when Storage Permission is granted by the system.
     * Attempts to create the app's working directory.
     */
    fun onPermissionGranted() {
        viewModelScope.launch {
            val created = AppFileManager.createAppFolder(getApplication())
            _uiState.value = _uiState.value.copy(folderCreated = created)
        }
    }

    /**
     * Called when Storage Permission is denied.
     * Attempts fallback creation of app-specific storage which requires no permission.
     */
    fun onPermissionDenied() {
        viewModelScope.launch {
            val created = AppFileManager.createAppFolder(getApplication())
            _uiState.value = _uiState.value.copy(folderCreated = created)
        }
    }

    // endregion

    // region Submission

    /**
     * Validates input and persists the user profile.
     */
    fun submitSurvey() {
        val currentState = _uiState.value
        
        if (isValidInput(currentState)) {
            _uiState.value = currentState.copy(isLoading = true)
            
            // Persist data using PreferenceManager
            preferenceManager.saveUserData(
                hobby = currentState.hobby,
                grade = currentState.grade,
                country = currentState.country,
                likes = currentState.likes,
                dislikes = currentState.dislikes
            )

            // Update state to trigger navigation in UI
            _uiState.value = currentState.copy(
                isLoading = false,
                isComplete = true
            )
        }
    }

    private fun isValidInput(state: SurveyUiState): Boolean {
        // Basic validation: Ensure required fields are not empty
        return state.hobby.isNotBlank() && 
               state.grade.isNotBlank() && 
               state.country.isNotBlank()
    }

    // endregion
}
```