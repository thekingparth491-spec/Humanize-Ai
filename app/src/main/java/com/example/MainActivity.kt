package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.HumanizeAiTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            HumanizeAiTheme(darkTheme = settings.darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyBackground
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
