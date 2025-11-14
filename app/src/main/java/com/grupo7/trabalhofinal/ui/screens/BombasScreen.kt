package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    val produtos by viewModel.produtos.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var bombaParaEditar by remember { mutableStateOf<Bomba?>(null) }

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
                            bombaParaEditar = bomba
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
            onAdd = { tipo, preco ->
                viewModel.novoTipo = tipo
                viewModel.novoPreco = preco
                viewModel.addBomba()
                showAdd = false
            },
            onDismiss = { showAdd = false },
            proximoId = bombas.size + 1,
            produtos = produtos
        )
    }

    bombaParaEditar?.let { bomba ->
        EditBombaDialog(
            bomba = bomba,
            produtos = produtos,
            onSave = { tipoAtualizado, precoAtualizado, statusAtualizado, produtoIdAtualizado ->
                android.util.Log.d("BombasScreen", "Salvando bomba com tipo: '$tipoAtualizado', produtoId: $produtoIdAtualizado")
                val precoDouble = precoAtualizado.toDoubleOrNull() ?: bomba.preco
                viewModel.updateBomba(
                    bomba.copy(
                        tipoCombustivel = tipoAtualizado,
                        preco = precoDouble,
                        status = statusAtualizado,
                        produtoId = produtoIdAtualizado
                    )
                )
                bombaParaEditar = null
            },
            onDismiss = { bombaParaEditar = null }
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
private fun AddBombaDialog(
    onAdd: (String, String) -> Unit,
    onDismiss: () -> Unit,
    proximoId: Int,
    produtos: List<com.grupo7.trabalhofinal.data.local.model.Produto>
) {
    var produtoSelecionado by remember { mutableStateOf(produtos.firstOrNull()) }
    var preco by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Bomba") },
        text = {
            Column {
                Text("Identificador: Bomba $proximoId", style = MaterialTheme.typography.body1, modifier = Modifier.padding(bottom = 8.dp))
                
                // Dropdown para tipo de combustível do estoque
                if (produtos.isEmpty()) {
                    Text("Nenhum produto em estoque. Adicione combustíveis primeiro.", color = MaterialTheme.colors.error)
                } else {
                    Box {
                        OutlinedTextField(
                            value = produtoSelecionado?.nome ?: "Selecione",
                            onValueChange = {},
                            label = { Text("Tipo de Combustível") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Expandir")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            produtos.forEach { produto ->
                                DropdownMenuItem(onClick = {
                                    produtoSelecionado = produto
                                    expanded = false
                                }) {
                                    Text("${produto.nome} (${produto.quantidade}L)")
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = preco,
                    onValueChange = { preco = it },
                    label = { Text("Preço (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    produtoSelecionado?.let { produto ->
                        onAdd(produto.nome, preco)
                    }
                },
                enabled = produtos.isNotEmpty() && produtoSelecionado != null
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditBombaDialog(
    bomba: Bomba,
    produtos: List<com.grupo7.trabalhofinal.data.local.model.Produto>,
    onSave: (String, String, String, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var produtoSelecionado by remember { 
        mutableStateOf(produtos.find { it.id == bomba.produtoId } ?: produtos.firstOrNull())
    }
    var preco by remember { mutableStateOf(bomba.preco.toString()) }
    var status by remember { mutableStateOf(bomba.status) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    
    val statusOpcoes = listOf("ativa", "manutencao", "inativa")
    
    android.util.Log.d("BombasScreen", "EditBombaDialog - Produtos disponíveis: ${produtos.map { "${it.id}:${it.nome}" }}")
    android.util.Log.d("BombasScreen", "EditBombaDialog - Produto atual da bomba: ID=${bomba.produtoId}, nome='${bomba.tipoCombustivel}'")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Bomba") },
        text = {
            Column {
                Text("Identificador: ${bomba.identificador}", style = MaterialTheme.typography.body1, modifier = Modifier.padding(bottom = 8.dp))
                
                // Dropdown para tipo de combustível do estoque
                if (produtos.isEmpty()) {
                    Text("Nenhum produto em estoque. Cadastre combustíveis primeiro.", color = MaterialTheme.colors.error)
                } else {
                    Text("Selecione o combustível:", style = MaterialTheme.typography.caption, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        OutlinedTextField(
                            value = produtoSelecionado?.nome ?: "Selecione",
                            onValueChange = {},
                            label = { Text("Tipo de Combustível") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedTipo = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Expandir")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { expandedTipo = false }
                        ) {
                            produtos.forEach { produto ->
                                DropdownMenuItem(onClick = {
                                    produtoSelecionado = produto
                                    expandedTipo = false
                                }) {
                                    Text("${produto.nome} (${produto.quantidade}L)")
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = preco,
                    onValueChange = { preco = it },
                    label = { Text("Preço (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Dropdown para status
                Box {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        label = { Text("Status") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedStatus = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Expandir")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOpcoes.forEach { opcao ->
                            DropdownMenuItem(onClick = {
                                status = opcao
                                expandedStatus = false
                            }) {
                                Text(opcao)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                produtoSelecionado?.let { produto ->
                    onSave(produto.nome, preco, status, produto.id)
                }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
