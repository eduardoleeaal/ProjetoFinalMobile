package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.data.local.model.Usuario
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.viewmodel.VendasViewModel
import kotlinx.coroutines.launch

@Composable
fun VendasScreen(
    viewModel: VendasViewModel,
    onBack: () -> Unit
) {
    val vendas by viewModel.vendas.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val bombas by viewModel.bombas.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var expandedUsuarios by remember { mutableStateOf(false) }
    var expandedBombas by remember { mutableStateOf(false) }
    var expandedPagamento by remember { mutableStateOf(false) }
    val formasPagamento = listOf("Dinheiro", "Pix", "Cartão de crédito")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vendas") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") }
            })
        },
        scaffoldState = scaffoldState
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Dropdown de bombas
            Text("Bomba", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.height(4.dp))
            Box {
                OutlinedButton(
                    onClick = { expandedBombas = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bombas.find { it.id == viewModel.selectedBombaId }?.let { 
                                "${it.identificador} - ${it.tipoCombustivel} (R$ ${it.preco}/L)"
                            } ?: "Selecione a bomba",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expandedBombas,
                    onDismissRequest = { expandedBombas = false }
                ) {
                    bombas.forEach { bomba ->
                        DropdownMenuItem(onClick = {
                            viewModel.selectedBombaId = bomba.id
                            expandedBombas = false
                        }) {
                            Text("${bomba.identificador} - ${bomba.tipoCombustivel} (R$ ${bomba.preco}/L)")
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            
            // Dropdown de clientes
            Text("Cliente", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.height(4.dp))
            Box {
                OutlinedButton(
                    onClick = { expandedUsuarios = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = usuarios.find { it.id == viewModel.selectedUsuarioId }?.nome ?: "Selecione o cliente",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expandedUsuarios,
                    onDismissRequest = { expandedUsuarios = false }
                ) {
                    DropdownMenuItem(onClick = {
                        viewModel.selectedUsuarioId = null
                        expandedUsuarios = false
                    }) {
                        Text("Nenhum (cliente não cadastrado)")
                    }
                    usuarios.forEach { usuario ->
                        DropdownMenuItem(onClick = {
                            viewModel.selectedUsuarioId = usuario.id
                            expandedUsuarios = false
                        }) {
                            Text("${usuario.nome} (${usuario.email})")
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            
            // Form de venda
            OutlinedTextField(
                value = viewModel.litrosInput,
                onValueChange = { viewModel.litrosInput = it },
                label = { Text("Litros") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            
            // Dropdown de forma de pagamento
            Text("Forma de Pagamento", style = MaterialTheme.typography.subtitle2)
            Spacer(Modifier.height(4.dp))
            Box {
                OutlinedButton(
                    onClick = { expandedPagamento = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.pagamentoInput,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expandedPagamento,
                    onDismissRequest = { expandedPagamento = false }
                ) {
                    formasPagamento.forEach { forma ->
                        DropdownMenuItem(onClick = {
                            viewModel.pagamentoInput = forma
                            expandedPagamento = false
                        }) {
                            Text(forma)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.registerVenda() }, modifier = Modifier.fillMaxWidth()) {
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
                        VendaRow(
                            venda = venda,
                            onDelete = { viewModel.deleteVenda(venda) }
                        )
                    }
                }
            }
        }
    }

    // Simple feedback (error)
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { errorMsg ->
            scaffoldState.snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = if (errorMsg.contains("✅")) SnackbarDuration.Short else SnackbarDuration.Long
            )
            viewModel.error = null
        }
    }
    
    // Feedback de loading
    if (viewModel.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun VendaRow(
    venda: Venda,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ID: ${venda.id}", style = MaterialTheme.typography.subtitle2)
                venda.usuarioId?.let {
                    Text("Cliente: $it", style = MaterialTheme.typography.caption)
                }
                Text("Litros: ${venda.litros} • R$ ${venda.valor}")
                Text("Pagamento: ${venda.pagamento}", style = MaterialTheme.typography.caption)
                Text("Synced: ${venda.synced}", style = MaterialTheme.typography.caption)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir venda",
                    tint = MaterialTheme.colors.error
                )
            }
        }
    }
}
