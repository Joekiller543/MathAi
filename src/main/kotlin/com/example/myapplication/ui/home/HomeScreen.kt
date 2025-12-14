package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome to My Application!")
        Text("Current User: ${viewModel.currentUser.value?.name ?: "Loading..."}")
    }
}