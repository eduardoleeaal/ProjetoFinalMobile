package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.viewmodel.VendasViewModel
import kotlinx.coroutines.launch

@Composable
fun VendasScreen(
    viewModel: VendasViewModel,
    onBack: () -> Unit
) {
    val vendas by viewModel.vendas.collectAsState()
    val bombas = viewModel // if you need bomba list, you'd pass BombasViewModel; simplified here
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vendas") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // exemplo simples: registra venda rápida (para demo)
                viewModel.litrosInput = "1.0"
                viewModel.registerVenda(usuarioId = null)
            }) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Form simplificado
            OutlinedTextField(
                value = viewModel.litrosInput,
                onValueChange = { viewModel.litrosInput = it },
                label = { Text("Litros") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.pagamentoInput,
                onValueChange = { viewModel.pagamentoInput = it },
                label = { Text("Forma de pagamento") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.registerVenda(usuarioId = null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar venda")
            }

            Spacer(Modifier.height(16.dp))
            Text("Histórico de vendas", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))

            if (vendas.isEmpty()) {
                Text("Sem vendas registradas.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vendas) { venda ->
                        VendaRow(venda = venda)
                    }
                }
            }
        }
    }

    // Simple feedback (error)
    val error by remember { derivedStateOf { viewModel.error } }
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                // no scaffold for Vendas; just log or ignore for now
            }
        }
    }
}

@Composable
private fun VendaRow(venda: Venda) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("ID: ${venda.id}", style = MaterialTheme.typography.subtitle2)
            Text("Litros: ${venda.litros} • R$ ${venda.valor}")
            Text("Pagamento: ${venda.pagamento}", style = MaterialTheme.typography.caption)
            Text("Synced: ${venda.synced}", style = MaterialTheme.typography.caption)
        }
    }
}
