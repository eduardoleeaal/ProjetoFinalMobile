package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Bomba
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BombasViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    // Lista de bombas exposta como State (via StateFlow)
    val bombas = localRepository.getBombas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Lista de produtos (combustíveis) em estoque
    val produtos = localRepository.getProdutos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Campos para formulário (exemplos)
    var novoTipo by mutableStateOf("")
    var novoPreco by mutableStateOf("")

    fun addBomba() {
        val preco = novoPreco.toDoubleOrNull() ?: run {
            error = "Preço inválido"
            return
        }
        
        // Encontrar o produto pelo nome
        val produto = produtos.value.find { it.nome == novoTipo }

        viewModelScope.launch {
            loading = true
            error = null
            try {
                val proximoNumero = bombas.value.size + 1
                val bomba = Bomba(
                    identificador = "Bomba $proximoNumero",
                    tipoCombustivel = novoTipo.ifBlank { "Gasolina" },
                    preco = preco,
                    status = "ativa",
                    produtoId = produto?.id // Salvar ID do produto
                )
                
                android.util.Log.d("BombasViewModel", "Criando bomba com produtoId=${produto?.id}, nome=${novoTipo}")
                
                // 1. Salvar localmente
                localRepository.insertBomba(bomba)
                android.util.Log.d("BombasViewModel", "Bomba salva no Room: $bomba")
                
                // 2. Enviar ao Firestore
                try {
                    val bombaData = mapOf(
                        "identificador" to bomba.identificador,
                        "tipoCombustivel" to bomba.tipoCombustivel,
                        "preco" to bomba.preco,
                        "status" to bomba.status
                    )
                    val result = remoteRepository.uploadBombaRemote(bombaData)
                    if (result.isSuccess) {
                        val docId = result.getOrNull()
                        android.util.Log.d("BombasViewModel", "✅ Bomba enviada ao Firestore! DocID: $docId")
                    } else {
                        android.util.Log.e("BombasViewModel", "❌ Erro ao enviar bomba: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BombasViewModel", "❌ Exceção ao enviar bomba: ${e.message}")
                }
                
                // limpar campos
                novoTipo = ""
                novoPreco = ""
            } catch (e: Exception) {
                error = e.message ?: "Erro ao inserir bomba."
                android.util.Log.e("BombasViewModel", "Erro ao inserir bomba: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun updateBomba(bomba: Bomba) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                android.util.Log.d("BombasViewModel", "=== ATUALIZANDO BOMBA ===")
                android.util.Log.d("BombasViewModel", "ID: ${bomba.id}")
                android.util.Log.d("BombasViewModel", "Identificador: ${bomba.identificador}")
                android.util.Log.d("BombasViewModel", "Tipo Combustível: '${bomba.tipoCombustivel}'")
                android.util.Log.d("BombasViewModel", "Produto ID: ${bomba.produtoId}")
                android.util.Log.d("BombasViewModel", "Preço: ${bomba.preco}")
                android.util.Log.d("BombasViewModel", "Status: ${bomba.status}")
                
                // 1. Atualizar localmente
                localRepository.updateBomba(bomba)
                android.util.Log.d("BombasViewModel", "✅ Bomba atualizada no Room!")
                
                // 2. Enviar ao Firestore (cria novo documento ou atualiza)
                try {
                    val bombaData = mapOf(
                        "id" to bomba.id,
                        "identificador" to bomba.identificador,
                        "tipoCombustivel" to bomba.tipoCombustivel,
                        "preco" to bomba.preco,
                        "status" to bomba.status
                    )
                    val result = remoteRepository.uploadBombaRemote(bombaData)
                    if (result.isSuccess) {
                        android.util.Log.d("BombasViewModel", "✅ Bomba atualizada no Firestore!")
                    } else {
                        android.util.Log.e("BombasViewModel", "❌ Erro ao atualizar bomba: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BombasViewModel", "❌ Exceção ao atualizar bomba: ${e.message}")
                }
            } catch (e: Exception) {
                error = e.message ?: "Erro ao atualizar bomba."
                android.util.Log.e("BombasViewModel", "Erro ao atualizar bomba: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun deleteBomba(bomba: Bomba) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.deleteBomba(bomba)
                android.util.Log.d("BombasViewModel", "✅ Bomba deletada do Room: ${bomba.identificador}")
                // Nota: Delete do Firestore requer o Document ID, que não temos mapeado
                // Por enquanto, apenas deletamos localmente
            } catch (e: Exception) {
                error = e.message ?: "Erro ao deletar bomba."
                android.util.Log.e("BombasViewModel", "Erro ao deletar bomba: ${e.message}")
            } finally {
                loading = false
            }
        }
    }
}