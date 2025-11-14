package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.viewmodel.EstoqueViewModel
import kotlinx.coroutines.launch

@Composable
fun EstoqueScreen(
    viewModel: EstoqueViewModel,
    onBack: () -> Unit
) {
    val produtos by viewModel.produtos.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var produtoEdit by remember { mutableStateOf<Produto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Estoque") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
            })
        },
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (produtos.isEmpty()) {
                Text("Nenhum produto no estoque.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(produtos) { produto ->
                        ProdutoRow(
                            produto = produto,
                            onEdit = { produtoEdit = produto },
                            onDelete = {
                                viewModel.deleteProduto(produto)
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Produto removido")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddProdutoDialog(onAdd = { nome, qtd, pc ->
            viewModel.novoNome = nome
            viewModel.novaQuantidade = qtd
            viewModel.novoPrecoCusto = pc
            viewModel.addProduto()
            showAdd = false
        }, onDismiss = { showAdd = false })
    }
    
    produtoEdit?.let { produto ->
        EditProdutoDialog(
            produto = produto,
            onEdit = { novaQtd, novoPrecoCusto ->
                val atualizado = produto.copy(
                    quantidade = novaQtd.toIntOrNull() ?: produto.quantidade,
                    precoCusto = novoPrecoCusto.toDoubleOrNull() ?: produto.precoCusto
                )
                viewModel.updateProduto(atualizado)
                produtoEdit = null
            },
            onDismiss = { produtoEdit = null }
        )
    }
}

@Composable
private fun ProdutoRow(produto: Produto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(produto.nome, style = MaterialTheme.typography.subtitle1)
                Text("Qtd: ${produto.quantidade} • Custo: R$ ${produto.precoCusto}", style = MaterialTheme.typography.body2)
            }
            Row {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun AddProdutoDialog(onAdd: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("1") }
    var precoCusto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Produto") },
        text = {
            Column {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = precoCusto,
                    onValueChange = { precoCusto = it },
                    label = { Text("Preço custo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(nome, quantidade, precoCusto) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditProdutoDialog(produto: Produto, onEdit: (String, String) -> Unit, onDismiss: () -> Unit) {
    var quantidade by remember { mutableStateOf(produto.quantidade.toString()) }
    var precoCusto by remember { mutableStateOf(produto.precoCusto.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ${produto.nome}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = precoCusto,
                    onValueChange = { precoCusto = it },
                    label = { Text("Preço custo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onEdit(quantidade, precoCusto) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
