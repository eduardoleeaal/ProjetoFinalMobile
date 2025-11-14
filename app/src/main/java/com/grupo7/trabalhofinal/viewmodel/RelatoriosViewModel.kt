package com.grupo7.trabalhofinal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CombustivelVendido(
    val nome: String,
    val litros: Double
)

class RelatoriosViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _totalVendas = MutableStateFlow(0)
    val totalVendas: StateFlow<Int> = _totalVendas.asStateFlow()

    private val _valorTotalVendas = MutableStateFlow(0.0)
    val valorTotalVendas: StateFlow<Double> = _valorTotalVendas.asStateFlow()

    private val _combustiveisVendidos = MutableStateFlow<List<CombustivelVendido>>(emptyList())
    val combustiveisVendidos: StateFlow<List<CombustivelVendido>> = _combustiveisVendidos.asStateFlow()

    private val _lucro = MutableStateFlow(0.0)
    val lucro: StateFlow<Double> = _lucro.asStateFlow()

    private val _produtosBaixoEstoque = MutableStateFlow<List<Produto>>(emptyList())
    val produtosBaixoEstoque: StateFlow<List<Produto>> = _produtosBaixoEstoque.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()
    
    private val _usuarioEmail = MutableStateFlow("")
    val usuarioEmail: StateFlow<String> = _usuarioEmail.asStateFlow()
    
    private val _usuarioNome = MutableStateFlow<String?>(null)
    val usuarioNome: StateFlow<String?> = _usuarioNome.asStateFlow()
    
    fun setUsuarioEmail(email: String) {
        _usuarioEmail.value = email
    }

    fun carregarRelatorios() {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            _usuarioNome.value = null

            try {
                val bombasList = localRepository.getBombas().first()
                val produtosList = localRepository.getProdutos().first()
                
                // Se email fornecido, buscar por usuário específico
                val vendasList = if (_usuarioEmail.value.isNotBlank()) {
                    // Buscar usuário pelo email
                    val usuarios = localRepository.getUsuarios().first()
                    val usuario = usuarios.find { it.email.equals(_usuarioEmail.value.trim(), ignoreCase = true) }
                    
                    if (usuario == null) {
                        _erro.value = "Usuário com email '${_usuarioEmail.value}' não encontrado"
                        android.util.Log.e("RelatoriosViewModel", "❌ Usuário não encontrado")
                        _isLoading.value = false
                        return@launch
                    }
                    
                    _usuarioNome.value = usuario.nome
                    android.util.Log.d("RelatoriosViewModel", "🔍 Filtrando vendas do usuário: ${usuario.nome} (${usuario.email})")
                    localRepository.getVendasByUsuarioId(usuario.id)
                } else {
                    // Buscar todas as vendas
                    localRepository.getVendas().first()
                }

                android.util.Log.d("RelatoriosViewModel", "📊 Vendas: ${vendasList.size}, Bombas: ${bombasList.size}, Produtos: ${produtosList.size}")

                // 1. Total de vendas e valor total
                _totalVendas.value = vendasList.size
                _valorTotalVendas.value = vendasList.sumOf { it.valor }

                // 2. Quantidade vendida por combustível
                val combustiveisMapa = mutableMapOf<String, Double>()
                vendasList.forEach { venda ->
                    val bomba = bombasList.find { it.id == venda.bombaId }
                    if (bomba != null) {
                        val combustivel = bomba.tipoCombustivel
                        combustiveisMapa[combustivel] = (combustiveisMapa[combustivel] ?: 0.0) + venda.litros
                    }
                }
                _combustiveisVendidos.value = combustiveisMapa.map { 
                    CombustivelVendido(it.key, it.value) 
                }.sortedByDescending { it.litros }

                // 3. Calcular lucro: (valor total vendido) - (custo total)
                // Custo total = soma de (precoCusto * litros vendidos) de cada venda
                var custoTotal = 0.0
                vendasList.forEach { venda ->
                    val bomba = bombasList.find { it.id == venda.bombaId }
                    if (bomba != null && bomba.produtoId != null) {
                        val produto = produtosList.find { it.id == bomba.produtoId }
                        if (produto != null) {
                            custoTotal += produto.precoCusto * venda.litros
                        }
                    }
                }
                _lucro.value = _valorTotalVendas.value - custoTotal

                // 4. Produtos com estoque baixo (quantidade <= 10)
                _produtosBaixoEstoque.value = produtosList.filter { it.quantidade <= 10 }

                android.util.Log.d("RelatoriosViewModel", "✅ Relatórios carregados: Total=${_totalVendas.value}, Valor=${_valorTotalVendas.value}, Lucro=${_lucro.value}")

            } catch (e: Exception) {
                _erro.value = "Erro ao carregar relatórios: ${e.message}"
                android.util.Log.e("RelatoriosViewModel", "❌ Erro: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
