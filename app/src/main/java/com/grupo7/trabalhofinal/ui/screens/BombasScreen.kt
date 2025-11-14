package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.clickable
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
import com.grupo7.trabalhofinal.data.local.model.Bomba
import com.grupo7.trabalhofinal.viewmodel.BombasViewModel
import kotlinx.coroutines.launch

@Composable
fun BombasScreen(
    viewModel: BombasViewModel,
    onBack: () -> Unit
) {
    val bombas by viewModel.bombas.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bombas") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
            })
        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (bombas.isEmpty()) {
                Text("Nenhuma bomba cadastrada.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bombas) { bomba ->
                        BombaRow(bomba = bomba, onEdit = {
                            // exemplo: alterar status
                            viewModel.updateBomba(bomba.copy(status = if (bomba.status == "ativa") "manutencao" else "ativa"))
                        }, onDelete = {
                            viewModel.deleteBomba(bomba)
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Bomba removida")
                            }
                        })
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddBombaDialog(
            onAdd = { identificador, tipo, preco ->
                viewModel.novoIdentificador = identificador
                viewModel.novoTipo = tipo
                viewModel.novoPreco = preco
                viewModel.addBomba()
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun BombaRow(bomba: Bomba, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(bomba.identificador, style = MaterialTheme.typography.subtitle1)
                Text("${bomba.tipoCombustivel} • R$ ${bomba.preco}", style = MaterialTheme.typography.body2)
                Text("Status: ${bomba.status}", style = MaterialTheme.typography.caption)
            }
            Row {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun AddBombaDialog(onAdd: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Bomba") },
        text = {
            Column {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Identificador") })
                OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo") })
                OutlinedTextField(value = preco, onValueChange = { preco = it }, label = { Text("Preço") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(id, tipo, preco) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
