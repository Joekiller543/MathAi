Here are the updated and complete Kotlin files implementing the User Survey and Onboarding UI with the required form fields (Hobby, Grade, Country, Likes, Dislikes) and Storage Permission handling.

File: app/src/main/java/com/example/myapplication/data/local/PreferenceManager.kt
```kotlin
package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages local data persistence using SharedPreferences.
 * Stores survey completion status and detailed user profile.
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_preferences"
        private const val KEY_IS_SETUP_DONE = "is_setup_done"
        private const val KEY_HOBBY = "user_hobby"
        private const val KEY_GRADE = "user_grade"
        private const val KEY_COUNTRY = "user_country"
        private const val KEY_LIKES = "user_likes"
        private const val KEY_DISLIKES = "user_dislikes"
    }

    fun isSetupDone(): Boolean {
        return prefs.getBoolean(KEY_IS_SETUP_DONE, false)
    }

    fun saveUserData(
        hobby: String, 
        grade: String, 
        country: String, 
        likes: String, 
        dislikes: String
    ) {
        prefs.edit().apply {
            putString(KEY_HOBBY, hobby)
            putString(KEY_GRADE, grade)
            putString(KEY_COUNTRY, country)
            putString(KEY_LIKES, likes)
            putString(KEY_DISLIKES, dislikes)
            putBoolean(KEY_IS_SETUP_DONE, true)
            apply()
        }
    }

    data class UserProfile(
        val hobby: String,
        val grade: String,
        val country: String,
        val likes: String,
        val dislikes: String
    )

    fun getUserData(): UserProfile {
        return UserProfile(
            hobby = prefs.getString(KEY_HOBBY, "") ?: "",
            grade = prefs.getString(KEY_GRADE, "") ?: "",
            country = prefs.getString(KEY_COUNTRY, "") ?: "",
            likes = prefs.getString(KEY_LIKES, "") ?: "",
            dislikes = prefs.getString(KEY_DISLIKES, "") ?: ""
        )
    }
}
```

File: app/src/main/java/com/example/myapplication/util/AppFileManager.kt
```kotlin
package com.example.myapplication.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

/**
 * Utility class to handle File System operations.
 * Responsible for creating the application's working directory.
 */
object AppFileManager {

    private const val FOLDER_NAME = "DevForgeProjectData"
    private const val TAG = "AppFileManager"

    /**
     * Creates the app-specific folder.
     * 1. Tries the public Documents folder (requires permissions on older Android).
     * 2. Falls back to App-Specific External Storage (always accessible).
     */
    fun createAppFolder(context: Context): Boolean {
        return try {
            var folderCreated = false
            
            // Strategy 1: Public Documents (Primary target for user visibility)
            // Note: On Android 11+ (API 30+), writing here via File API is restricted 
            // without MANAGE_EXTERNAL_STORAGE, so this block effectively targets < API 30.
            val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appPublicFolder = File(publicDocs, FOLDER_NAME)
            
            if (!appPublicFolder.exists()) {
                if (appPublicFolder.mkdirs()) {
                    Log.d(TAG, "Created public folder: ${appPublicFolder.absolutePath}")
                    folderCreated = true
                }
            } else {
                folderCreated = true // Exists
            }

            // Strategy 2: App Specific Storage (Fallback & Modern Standard)
            // If public folder creation failed (e.g., API 30+ restrictions or denied permission),
            // ensure we at least have our internal scoped storage.
            if (!folderCreated) {
                val scopedFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FOLDER_NAME)
                if (!scopedFolder.exists()) {
                    scopedFolder.mkdirs()
                }
                Log.d(TAG, "Using scoped folder: ${scopedFolder.absolutePath}")
                folderCreated = true
            }

            folderCreated
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create app folder", e)
            false
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/ui/survey/SurveyViewModel.kt
```kotlin
package com.example.myapplication.ui.survey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.util.AppFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class SurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    
    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    init {
        checkStartupStatus()
    }

    private fun checkStartupStatus() {
        val isDone = preferenceManager.isSetupDone()
        _uiState.value = _uiState.value.copy(isComplete = isDone)
    }

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

    /**
     * Called when Storage Permission is granted.
     * Attempts to create the folder.
     */
    fun onPermissionGranted() {
        viewModelScope.launch {
            val created = AppFileManager.createAppFolder(getApplication())
            _uiState.value = _uiState.value.copy(folderCreated = created)
        }
    }

    fun onPermissionDenied() {
        // Fallback: Try creating app-specific folder anyway, as it doesn't need permission
        viewModelScope.launch {
            val created = AppFileManager.createAppFolder(getApplication())
            _uiState.value = _uiState.value.copy(folderCreated = created)
        }
    }

    fun submitSurvey() {
        val currentState = _uiState.value
        // Basic validation
        if (isValidInput(currentState)) {
            
            _uiState.value = currentState.copy(isLoading = true)
            
            // Save to Persistence
            preferenceManager.saveUserData(
                hobby = currentState.hobby,
                grade = currentState.grade,
                country = currentState.country,
                likes = currentState.likes,
                dislikes = currentState.dislikes
            )

            // Transition to complete state
            _uiState.value = currentState.copy(
                isLoading = false,
                isComplete = true
            )
        }
    }

    private fun isValidInput(state: SurveyUiState): Boolean {
        return state.hobby.isNotBlank() && 
               state.grade.isNotBlank() && 
               state.country.isNotBlank()
    }
}
```

File: app/src/main/java/com/example/myapplication/ui/survey/SurveyScreen.kt
```kotlin
package com.example.myapplication.ui.survey

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SurveyScreen(viewModel: SurveyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            Toast.makeText(context, "Using app-specific storage (Permission denied)", Toast.LENGTH_SHORT).show()
            viewModel.onPermissionDenied()
        }
    }

    // Trigger permission request on first load
    LaunchedEffect(Unit) {
        // WRITE_EXTERNAL_STORAGE is deprecated in API 29/30+, but needed for legacy folder creation logic
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            // Newer Android versions handle scoped storage automatically for app dirs
            viewModel.onPermissionGranted()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Profile Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Tell us about yourself to personalize your experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Form Fields
            SurveyTextField(
                value = uiState.hobby,
                onValueChange = viewModel::onHobbyChange,
                label = "Your Hobby"
            )

            SurveyTextField(
                value = uiState.grade,
                onValueChange = viewModel::onGradeChange,
                label = "Grade / Level"
            )

            SurveyTextField(
                value = uiState.country,
                onValueChange = viewModel::onCountryChange,
                label = "Country"
            )
            
            SurveyTextField(
                value = uiState.likes,
                onValueChange = viewModel::onLikesChange,
                label = "Likes (e.g. Coding, Math)",
                singleLine = false
            )

            SurveyTextField(
                value = uiState.dislikes,
                onValueChange = viewModel::onDislikesChange,
                label = "Dislikes (e.g. History, Bugs)",
                singleLine = false,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.submitSurvey() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = uiState.hobby.isNotBlank() && uiState.grade.isNotBlank() && uiState.country.isNotBlank()
                ) {
                    Text("Complete Setup")
                }
            }

            if (uiState.folderCreated) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "✓ Storage Environment Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun SurveyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction)
    )
}
```

File: app/src/main/java/com/example/myapplication/MainActivity.kt
```kotlin
package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.survey.SurveyScreen
import com.example.myapplication.ui.survey.SurveyViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationEntry()
                }
            }
        }
    }
}

@Composable
fun AppNavigationEntry() {
    // Scoped to the Activity lifecycle
    val surveyViewModel: SurveyViewModel = viewModel()
    val uiState by surveyViewModel.uiState.collectAsState()

    if (uiState.isComplete) {
        // Navigate to Main App Content
        MainContent(
            hobby = uiState.hobby,
            grade = uiState.grade,
            likes = uiState.likes
        )
    } else {
        // Show Onboarding/Survey
        SurveyScreen(viewModel = surveyViewModel)
    }
}

@Composable
fun MainContent(hobby: String, grade: String, likes: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome!\n\nHobby: $hobby\nGrade: $grade\nLikes: $likes",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
```