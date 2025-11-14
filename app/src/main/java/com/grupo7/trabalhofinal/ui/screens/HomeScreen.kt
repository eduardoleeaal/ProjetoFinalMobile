package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    onNavigateToBombas: () -> Unit,
    onNavigateToVendas: () -> Unit,
    onNavigateToEstoque: () -> Unit,
    onNavigateToRelatorios: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posto - Painel") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Bem-vindo ao painel do posto", style = MaterialTheme.typography.h6)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToBombas) { Text("Bombas") }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToVendas) { Text("Vendas") }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToEstoque) { Text("Estoque") }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToRelatorios) { Text("Relatórios") }
        }
    }
}
