package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.viewmodel.RelatoriosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RelatoriosScreen(
    viewModel: RelatoriosViewModel,
    onBack: () -> Unit
) {
    val vendasTotais by viewModel.totalVendas.collectAsState()
    val valorTotal by viewModel.valorTotalVendas.collectAsState()
    val combustiveisVendidos by viewModel.combustiveisVendidos.collectAsState()
    val lucro by viewModel.lucro.collectAsState()
    val produtosBaixoEstoque by viewModel.produtosBaixoEstoque.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val usuarioEmail by viewModel.usuarioEmail.collectAsState()
    val usuarioNome by viewModel.usuarioNome.collectAsState()
    
    var emailInput by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    LaunchedEffect(Unit) {
        viewModel.carregarRelatorios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Erro
                erro?.let {
                    item {
                        Card(
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Erro: $it",
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                // Busca por Usuário
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🔍 Buscar por Cliente",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            usuarioNome?.let {
                                Text(
                                    "Relatório de: $it",
                                    style = MaterialTheme.typography.subtitle1,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email do cliente") },
                                    placeholder = { Text("exemplo@email.com") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.setUsuarioEmail(emailInput)
                                        viewModel.carregarRelatorios()
                                    },
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("Buscar")
                                }
                            }
                            
                            if (usuarioEmail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        emailInput = ""
                                        viewModel.setUsuarioEmail("") 
                                        viewModel.carregarRelatorios()
                                    }
                                ) {
                                    Text("Limpar filtro - Ver todos")
                                }
                            }
                        }
                    }
                }

                // Resumo de Vendas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "📊 Resumo de Vendas",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total de Vendas:", style = MaterialTheme.typography.caption)
                                    Text(
                                        "$vendasTotais",
                                        style = MaterialTheme.typography.h5,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    Text("Valor Total:", style = MaterialTheme.typography.caption)
                                    Text(
                                        currencyFormat.format(valorTotal),
                                        style = MaterialTheme.typography.h5,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Lucro
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        backgroundColor = if (lucro >= 0) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.error.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "💰 Lucro Total",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                currencyFormat.format(lucro),
                                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.Bold,
                                color = if (lucro >= 0) MaterialTheme.colors.primary else MaterialTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "(Valor Total - Custo Total)",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Combustíveis Vendidos
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⛽ Combustíveis Vendidos",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (combustiveisVendidos.isEmpty()) {
                                Text(
                                    "Nenhuma venda registrada.",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                combustiveisVendidos.forEach { combustivel ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            combustivel.nome,
                                            style = MaterialTheme.typography.body1,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "${String.format("%.2f", combustivel.litros)} L",
                                            style = MaterialTheme.typography.body1,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Produtos com Baixo Estoque
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Estoque Baixo (≤ 10 unidades)",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (produtosBaixoEstoque.isEmpty()) {
                                Text(
                                    "Nenhum produto com estoque baixo.",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Lista de produtos com estoque baixo
                items(produtosBaixoEstoque) { produto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    produto.nome,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Custo: ${currencyFormat.format(produto.precoCusto)}",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                            Text(
                                "${produto.quantidade} un.",
                                style = MaterialTheme.typography.h6,
                                color = if (produto.quantidade <= 5) MaterialTheme.colors.error else MaterialTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Botão de atualizar
                item {
                    Button(
                        onClick = { viewModel.carregarRelatorios() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Atualizar Relatórios")
                    }
                }
            }
        }
    }
}
