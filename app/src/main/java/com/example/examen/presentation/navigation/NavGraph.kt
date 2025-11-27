package com.example.examen.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.examen.presentation.screens.SudokuScreen

// Clase para las rutas
sealed class Screen(
    val route: String,
) {
    object SudokuScreen : Screen("sudoku")

}

// onBackClick = { navController.popBackStack() }
@Composable
fun ExamenNavGraph( // Va dentro del Main activity el ExamenNavGraph
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
){
    NavHost(
        navController = navController,
        startDestination = Screen.SudokuScreen.route,
        modifier = modifier,
    ){
        composable(route = Screen.SudokuScreen.route) {
            SudokuScreen(
                viewModel = hiltViewModel()
            )
        }
    }
}