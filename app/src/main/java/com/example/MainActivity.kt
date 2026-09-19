package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.LokSetuTheme
import com.example.ui.viewmodel.LokSetuViewModel

class MainActivity : ComponentActivity() {

  private val vm: LokSetuViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      LokSetuTheme {
        HomeScreen(viewModel = vm)
      }
    }
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (vm.sosManager.onHardwareKeyEvent(event.keyCode, event)) {
      vm.openSosDialog()
      return true
    }
    return super.dispatchKeyEvent(event)
  }
}

