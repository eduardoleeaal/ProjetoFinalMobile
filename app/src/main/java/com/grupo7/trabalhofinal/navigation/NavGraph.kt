package com.grupo7.trabalhofinal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grupo7.trabalhofinal.viewmodel.AuthViewModel
import com.grupo7.trabalhofinal.viewmodel.BombasViewModel
import com.grupo7.trabalhofinal.viewmodel.EstoqueViewModel
import com.grupo7.trabalhofinal.viewmodel.VendasViewModel
import com.grupo7.trabalhofinal.ui.screens.* // assumindo que as screens ficam em ui.screens

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    bombasViewModel: BombasViewModel,
    vendasViewModel: VendasViewModel,
    estoqueViewModel: EstoqueViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBombas = { navController.navigate(Screen.Bombas.route) },
                onNavigateToVendas = { navController.navigate(Screen.Vendas.route) },
                onNavigateToEstoque = { navController.navigate(Screen.Estoque.route) },
                onNavigateToRelatorios = { navController.navigate(Screen.Relatorios.route) },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Bombas.route) {
            BombasScreen(viewModel = bombasViewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Vendas.route) {
            VendasScreen(viewModel = vendasViewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Estoque.route) {
            EstoqueScreen(viewModel = estoqueViewModel, onBack = { navController.popBackStack() })
        }

        composable(Screen.Relatorios.route) {
            RelatoriosScreen(onBack = { navController.popBackStack() })
        }
    }
}
