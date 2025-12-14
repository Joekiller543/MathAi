Here is the updated `app/src/main/java/com/example/myapplication/MainActivity.kt` file.

I have replaced the basic `AppNavigation` composable with a robust `AppNavHost` implementation using Jetpack Compose Navigation. This sets up three distinct routes: `Onboarding`, `Learning`, and `Calculator`. It also handles the logic for automatically navigating from the Survey to the Main App once setup is complete.

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.CerebrasRepository
import com.example.myapplication.data.repository.MathRepository
import com.example.myapplication.ui.learning.LearningScreen
import com.example.myapplication.ui.learning.LearningViewModel
import com.example.myapplication.ui.learning.ScientificCalculator
import com.example.myapplication.ui.onboarding.SurveyScreen
import com.example.myapplication.ui.onboarding.SurveyViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection Setup
        val preferenceManager = PreferenceManager(applicationContext)
        val cerebrasRepository = CerebrasRepository(RetrofitClient.apiService)
        val mathRepository = MathRepository(cerebrasRepository, preferenceManager)

        // ViewModel Factory handles passing dependencies to ViewModels
        val appViewModelFactory = AppViewModelFactory(application, mathRepository, preferenceManager)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Determine initial destination based on persistent setup status
                    val startDestination = if (preferenceManager.isSetupDone()) {
                        Screen.Learning.route
                    } else {
                        Screen.Onboarding.route
                    }

                    AppNavHost(
                        navController = navController,
                        viewModelFactory = appViewModelFactory,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

/**
 * Type-safe definition of navigation routes.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Learning : Screen("learning")
    object Calculator : Screen("calculator")
}

/**
 * Unified NavHost managing navigation between the Onboarding/Survey screens,
 * the AI Learning interface, and the Calculator screen.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModelFactory: ViewModelProvider.Factory,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Route: Onboarding / Survey
        composable(Screen.Onboarding.route) {
            val surveyViewModel: SurveyViewModel = viewModel(factory = viewModelFactory)
            val surveyState by surveyViewModel.uiState.collectAsState()

            // Automatically navigate to Learning screen when survey is completed
            LaunchedEffect(surveyState.isComplete) {
                if (surveyState.isComplete) {
                    navController.navigate(Screen.Learning.route) {
                        // Clear backstack so user can't go back to onboarding
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }
            
            SurveyScreen(viewModel = surveyViewModel)
        }

        // Route: Main AI Learning Interface
        composable(Screen.Learning.route) {
            // ViewModel is scoped to this NavGraph destination
            val learningViewModel: LearningViewModel = viewModel(factory = viewModelFactory)
            LearningScreen(viewModel = learningViewModel)
        }

        // Route: Standalone Calculator
        composable(Screen.Calculator.route) {
            val learningViewModel: LearningViewModel = viewModel(factory = viewModelFactory)
            val uiState by learningViewModel.uiState.collectAsState()

            // Reuse the ScientificCalculator composable in a full-screen context
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                ScientificCalculator(
                    expression = uiState.calcExpression,
                    result = uiState.calcResult,
                    onInput = learningViewModel::onCalculatorInput,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Custom Factory to inject dependencies into ViewModels.
 */
class AppViewModelFactory(
    private val application: android.app.Application,
    private val mathRepository: MathRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurveyViewModel::class.java)) {
            return SurveyViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            // Inject both MathRepository and PreferenceManager into LearningViewModel
            return LearningViewModel(mathRepository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}