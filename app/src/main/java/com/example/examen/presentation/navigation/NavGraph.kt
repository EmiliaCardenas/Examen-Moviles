package com.example.examen.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.examen.presentation.screens.HomeScreen
import com.example.examen.presentation.screens.SudokuScreen

sealed class Screen(
    val route: String,
) {
    object HomeScreen : Screen("home")

    object SudokuScreen : Screen("sudoku?difficulty={difficulty}") {
        fun createRoute(difficulty: String) = "sudoku?difficulty=$difficulty"
    }

}

// onBackClick = { navController.popBackStack() }
@Composable
fun ExamenNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
){
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route,
        modifier = modifier
    ) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.SudokuScreen.route,
            arguments = listOf(
                navArgument("difficulty") { defaultValue = "medium" }
            )
        )  { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "medium"
            SudokuScreen(
                navController = navController,
                difficulty = difficulty
            )
        }

    }
}