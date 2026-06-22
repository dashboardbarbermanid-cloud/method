package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.KulturAppContainer
import com.example.ui.KulturViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: KulturViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Insets are beautifully handled by our custom scaffold layers in KulturApp.kt
          Box(modifier = Modifier.fillMaxSize()) {
            KulturAppContainer(viewModel)
          }
        }
      }
    }
  }
}
