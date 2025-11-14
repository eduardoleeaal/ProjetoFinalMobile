package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VendasViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    // Expor vendas locais (ordenadas pelo DAO)
    val vendas = localRepository.getVendas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Vendas não sincronizadas (útil para UI ou debug)
    val vendasUnsynced = localRepository.getVendasUnsynced()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Lista de usuários (clientes) para dropdown
    val usuarios = localRepository.getUsuarios()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Lista de bombas para dropdown
    val bombas = localRepository.getBombas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Lista de produtos (combustíveis) para validação de estoque
    val produtos = localRepository.getProdutos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)

    // Campos do formulário de venda
    var selectedBombaId by mutableStateOf<Long?>(null)
    var selectedUsuarioId by mutableStateOf<String?>(null)
    var litrosInput by mutableStateOf("")
    var pagamentoInput by mutableStateOf("Dinheiro")

    /**
     * Insere uma venda localmente (synced = false por padrão).
     * A sincronização ficará a cargo do SyncManager (ou podemos tentar subir imediatamente aqui).
     */
    fun registerVenda() {
        android.util.Log.d("VendasViewModel", "🔵 INICIANDO registerVenda()")
        
        val litros = litrosInput.toDoubleOrNull()
        android.util.Log.d("VendasViewModel", "Litros input: '$litrosInput' -> parsed: $litros")
        
        if (litros == null || litros <= 0) {
            error = "Litros inválidos"
            android.util.Log.e("VendasViewModel", "❌ Litros inválidos")
            return
        }
        
        android.util.Log.d("VendasViewModel", "selectedBombaId: $selectedBombaId")
        if (selectedBombaId == null) {
            error = "Selecione uma bomba"
            android.util.Log.e("VendasViewModel", "❌ Bomba não selecionada")
            return
        }

        android.util.Log.d("VendasViewModel", "🟢 Validações iniciais OK, iniciando coroutine")
        
        viewModelScope.launch {
            loading = true
            error = null
            try {
                android.util.Log.d("VendasViewModel", "📦 Produtos em estoque: ${produtos.value.map { "${it.nome} (${it.quantidade}L)" }}")
                
                // Buscar bomba selecionada para pegar o preço e tipo de combustível
                val bomba = bombas.value.find { it.id == selectedBombaId }
                android.util.Log.d("VendasViewModel", "Bombas disponíveis: ${bombas.value.map { "${it.id}:${it.identificador}" }}")
                
                if (bomba == null) {
                    error = "Bomba não encontrada"
                    android.util.Log.e("VendasViewModel", "❌ Bomba não encontrada com ID: $selectedBombaId")
                    loading = false
                    return@launch
                }
                
                android.util.Log.d("VendasViewModel", "⛽ Bomba selecionada: ${bomba.identificador} - tipoCombustivel='${bomba.tipoCombustivel}', produtoId=${bomba.produtoId}")
                
                // Calcular valor: litros * preço do combustível
                val valor = litros * bomba.preco
                
                android.util.Log.d("VendasViewModel", "💰 Valor calculado: ${litros}L x R$${bomba.preco} = R$$valor")
                android.util.Log.d("VendasViewModel", "Criando venda: bombaId=$selectedBombaId, usuarioId=$selectedUsuarioId, litros=$litros, valor=$valor, pagamento=$pagamentoInput")

                val venda = Venda(
                    bombaId = selectedBombaId,
                    usuarioId = selectedUsuarioId,
                    litros = litros,
                    valor = valor,
                    pagamento = pagamentoInput,
                    data = System.currentTimeMillis(),
                    synced = false
                )
                
                android.util.Log.d("VendasViewModel", "💾 Inserindo venda no banco...")
                val localId = localRepository.insertVenda(venda)
                android.util.Log.d("VendasViewModel", "✅ Venda inserida com ID: $localId")
                
                // Atualizar estoque se tiver produtoId
                if (bomba.produtoId != null) {
                    val produto = produtos.value.find { it.id == bomba.produtoId }
                    if (produto != null) {
                        android.util.Log.d("VendasViewModel", "📦 Atualizando estoque: ${produto.quantidade}L -> ${produto.quantidade - litros.toInt()}L")
                        val produtoAtualizado = produto.copy(quantidade = produto.quantidade - litros.toInt())
                        localRepository.updateProduto(produtoAtualizado)
                        android.util.Log.d("VendasViewModel", "✅ Estoque atualizado")
                    }
                }
                
                // Tentar upload imediato para Firestore
                try {
                    android.util.Log.d("VendasViewModel", "☁️ Enviando para Firestore...")
                    val novaVenda = venda.copy(id = localId)
                    val uploadResult = remoteRepository.uploadVenda(novaVenda)
                    if (uploadResult.isSuccess) {
                        val docId = uploadResult.getOrNull()
                        android.util.Log.d("VendasViewModel", "✅ Venda enviada ao Firestore! DocID: $docId")
                        localRepository.markVendaSynced(localId)
                    } else {
                        android.util.Log.e("VendasViewModel", "❌ Erro ao enviar venda: ${uploadResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("VendasViewModel", "❌ Exceção ao enviar venda: ${e.message}")
                    e.printStackTrace()
                }
                
                // Limpar campos após sucesso
                litrosInput = ""
                selectedBombaId = null
                selectedUsuarioId = null
                pagamentoInput = "Dinheiro"
                
                android.util.Log.d("VendasViewModel", "✅✅✅ VENDA REGISTRADA COM SUCESSO!")
                error = "✅ Venda registrada com sucesso!"
                
            } catch (e: Exception) {
                error = e.message ?: "Erro ao registrar venda."
                android.util.Log.e("VendasViewModel", "❌❌❌ Erro ao registrar venda: ${e.message}")
                e.printStackTrace()
            } finally {
                loading = false
                android.util.Log.d("VendasViewModel", "🔵 FIM registerVenda() - loading=false")
            }
        }
    }
    
    fun deleteVenda(venda: Venda) {
        viewModelScope.launch {
            try {
                android.util.Log.d("VendasViewModel", "🗑️ Deletando venda ID: ${venda.id}")
                localRepository.deleteVenda(venda)
                android.util.Log.d("VendasViewModel", "✅ Venda deletada")
                error = "✅ Venda excluída com sucesso!"
            } catch (e: Exception) {
                android.util.Log.e("VendasViewModel", "❌ Erro ao deletar venda: ${e.message}")
                error = "Erro ao excluir venda: ${e.message}"
            }
        }
    }
}