package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RelatoriosScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Relatórios") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
            })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Relatórios básicos", style = MaterialTheme.typography.h6)
            Text("• Vendas totais (implemente consulta Firestore no RemoteRepository)", style = MaterialTheme.typography.body1)
            Text("• Estoque baixo (produtos com pouca quantidade)", style = MaterialTheme.typography.body1)
        }
    }
}
