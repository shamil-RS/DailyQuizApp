package com.example.dailyquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyquiz.data.remote.ApiService
import com.example.dailyquiz.ui.theme.DailyQuizTheme
import com.example.dailyquiz.ui.theme.screen.online_quiz_screen.MainScreenViewModel
import com.example.dailyquiz.ui.theme.screen.quiz_screen.QuizScreen
import com.example.dailyquiz.ui.theme.screen.splash_screen.SplashScreen
import com.example.dailyquiz.ui.theme.screen.splash_screen.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyQuizTheme {
                val viewModel: MainScreenViewModel = viewModel()

                val splashViewModel: SplashViewModel by viewModels()
                val isLoading = splashViewModel.isLoading.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    LaunchedEffect(key1 = viewModel) {
                        if (isLoading.value) {
                            delay(3000L)

                            splashViewModel.splashScreen()
                        }
                    }

                    Crossfade(
                        targetState = isLoading.value,
                        animationSpec = tween(1500)
                    ) {
                        if (it) SplashScreen() else QuizScreen(viewModel)
                    }
                }
            }
        }
    }
}

