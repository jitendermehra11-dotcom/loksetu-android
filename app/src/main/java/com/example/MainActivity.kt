package com.aistudio.loksetu.vxqtmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.viewmodel.LokSetuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val lokSetuViewModel: LokSetuViewModel = viewModel()
            HomeScreen(viewModel = lokSetuViewModel)
        }
    }
}
