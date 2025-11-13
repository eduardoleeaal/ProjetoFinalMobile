package com.grupo7.trabalhofinal.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Bombas : Screen("bombas")
    object Vendas : Screen("vendas")
    object Estoque : Screen("estoque")
    object Relatorios : Screen("relatorios")
}