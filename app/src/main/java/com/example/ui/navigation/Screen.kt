package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object AiChecker : Screen("ai_checker")
    data object AiCheckerResult : Screen("ai_checker_result")
    data object Humanizer : Screen("humanizer")
    data object HumanizerResult : Screen("humanizer_result")
    data object AiStyle : Screen("ai_style")
    data object Improver : Screen("improver")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}
