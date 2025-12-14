package com.example.myapplication.ui.onboarding

data class OnboardingState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)