Here are the refactored and implemented files. I have extracted the Navigation logic and the ViewModelFactory into their own files to adhere to Clean Architecture principles, reduce the complexity of `MainActivity`, and resolve compilation visibility issues.

File: app/src/main/java/com/example/myapplication/navigation/AppNavHost.kt
```kotlin
package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.features.calculator.ui.CalculatorScreen
import com.example.myapplication.features.calculator.ui.CalculatorViewModel
import com.example.myapplication.ui.learning.LearningScreen
import com.example.myapplication.ui.learning.LearningViewModel
import com.example.myapplication.ui.onboarding.OnboardingScreen
import com.example.myapplication.ui.onboarding.OnboardingViewModel

/**
 * Type-safe definition of navigation routes.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Learning : Screen("learning")
    object Calculator : Screen("calculator")
}

/**
 * The central Navigation Host for the application.
 * Manages the graph and transitions between the three main features:
 * 1. Onboarding (Survey)
 * 2. Learning (Chat)
 * 3. Calculator (Utility)
 *
 * @param navController The NavHostController to manage stack navigation.
 * @param viewModelFactory The custom factory to inject dependencies into ViewModels.
 * @param startDestination The initial screen to render (determined by MainActivity).
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
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)
            val state by onboardingViewModel.uiState.collectAsState()

            // Observe completion state to navigate to Learning screen automatically
            LaunchedEffect(state.isComplete) {
                if (state.isComplete) {
                    navController.navigate(Screen.Learning.route) {
                        // Clear backstack so user can't return to onboarding via back button
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }
            
            OnboardingScreen(viewModel = onboardingViewModel)
        }

        // Route: Main AI Learning Interface
        composable(Screen.Learning.route) {
            val learningViewModel: LearningViewModel = viewModel(factory = viewModelFactory)
            LearningScreen(viewModel = learningViewModel)
        }

        // Route: Scientific Calculator Feature
        composable(Screen.Calculator.route) {
            val calculatorViewModel: CalculatorViewModel = viewModel(factory = viewModelFactory)
            
            CalculatorScreen(
                viewModel = calculatorViewModel,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/AppViewModelFactory.kt
```kotlin
package com.example.myapplication

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.MathRepository
import com.example.myapplication.features.calculator.data.CalculatorRepository
import com.example.myapplication.features.calculator.ui.CalculatorViewModel
import com.example.myapplication.ui.learning.LearningViewModel
import com.example.myapplication.ui.onboarding.OnboardingViewModel

/**
 * Custom ViewModelFactory to handle Dependency Injection for ViewModels.
 * 
 * This enables passing Repositories and Application Context into ViewModels
 * via the constructor, which is not supported by the default factory.
 */
class AppViewModelFactory(
    private val application: Application,
    private val mathRepository: MathRepository,
    private val preferenceManager: PreferenceManager,
    private val calculatorRepository: CalculatorRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(application) as T
            }
            modelClass.isAssignableFrom(LearningViewModel::class.java) -> {
                LearningViewModel(mathRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(CalculatorViewModel::class.java) -> {
                CalculatorViewModel(calculatorRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
```

File: app/src/main/java/com/example/myapplication/MainActivity.kt
```kotlin
package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.local.PreferenceManager
import com.example.myapplication.data.repository.CerebrasRepository
import com.example.myapplication.data.repository.MathRepository
import com.example.myapplication.features.calculator.data.CalculatorRepository
import com.example.myapplication.features.calculator.data.local.CalculationDataSource
import com.example.myapplication.navigation.AppNavHost
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * The Main Entry point of the application.
 * Responsible for:
 * 1. Initializing the Data Layer (Repositories, Data Sources).
 * 2. Setting up the AppViewModelFactory for Dependency Injection.
 * 3. Initializing the Navigation Host.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- Manual Dependency Injection Setup ---
        
        // 1. Core Data & Persistence
        val preferenceManager = PreferenceManager(applicationContext)
        val cerebrasRepository = CerebrasRepository(RetrofitClient.apiService)
        
        // 2. Feature Repositories
        val mathRepository = MathRepository(cerebrasRepository, preferenceManager)
        
        val calculationDataSource = CalculationDataSource()
        val calculatorRepository = CalculatorRepository(calculationDataSource)

        // 3. ViewModel Factory
        val appViewModelFactory = AppViewModelFactory(
            application = application,
            mathRepository = mathRepository,
            preferenceManager = preferenceManager,
            calculatorRepository = calculatorRepository
        )

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Determine initial destination based on user's setup status
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
```