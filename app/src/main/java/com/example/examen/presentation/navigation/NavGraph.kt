package com.example.examen.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Clase para las rutas
sealed class Screen(
    val route: String,
) {
    /**
     * Ejemplos
     *
     * object Login : Screen("login")
     *
     *
     * object WorkshopDetail : Screen("workshop/{id}") {
     *         fun createRoute(id: String): String = "workshop/$id"
     *     }
     *
     * object AddNewWorkshop : Screen("workshop/add")
     */

}

// onBackClick = { navController.popBackStack() }
@Composable
fun ExamenNavGraph( // Va dentro del Main activity el ExamenNavGraph
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
){
    NavHost(
        navController = navController,
        startDestination = //Screen..route, poner ruta de inicio de la app
        modifier = modifier,
    ){
        composable(route = // Ruta de la clase Screen) {
            NombreScreen( )
        }
    }
}