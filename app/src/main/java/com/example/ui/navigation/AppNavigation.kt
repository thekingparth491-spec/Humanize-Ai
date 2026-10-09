package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.aistyle.AiStyleScreen
import com.example.ui.checker.AiCheckerResultScreen
import com.example.ui.checker.AiCheckerScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.humanizer.HumanizerResultScreen
import com.example.ui.humanizer.HumanizerScreen
import com.example.ui.improver.TextImproveScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToChecker = { navController.navigate(Screen.AiChecker.route) },
                onNavigateToHumanizer = { navController.navigate(Screen.Humanizer.route) },
                onNavigateToAiStyle = { navController.navigate(Screen.AiStyle.route) },
                onNavigateToImprover = { navController.navigate(Screen.Improver.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.AiChecker.route) {
            AiCheckerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.AiCheckerResult.route) }
            )
        }

        composable(Screen.AiCheckerResult.route) {
            AiCheckerResultScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Humanizer.route) {
            HumanizerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.HumanizerResult.route) }
            )
        }

        composable(Screen.HumanizerResult.route) {
            HumanizerResultScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AiStyle.route) {
            AiStyleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Improver.route) {
            TextImproveScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChecker = { navController.navigate(Screen.AiChecker.route) },
                onNavigateToHumanizer = { navController.navigate(Screen.Humanizer.route) },
                onNavigateToAiStyle = { navController.navigate(Screen.AiStyle.route) },
                onNavigateToImprover = { navController.navigate(Screen.Improver.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
