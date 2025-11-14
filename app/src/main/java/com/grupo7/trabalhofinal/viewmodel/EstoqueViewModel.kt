package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EstoqueViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    val produtos = localRepository.getProdutos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Campos do formulário de produto
    var novoNome by mutableStateOf("")
    var novaQuantidade by mutableStateOf("0")
    var novoPrecoCusto by mutableStateOf("")

    fun addProduto() {
        val quantidade = novaQuantidade.toIntOrNull() ?: run {
            error = "Quantidade inválida"
            return
        }
        val precoCusto = novoPrecoCusto.toDoubleOrNull() ?: run {
            error = "Preço de custo inválido"
            return
        }

        val produto = Produto(
            nome = novoNome.ifBlank { "Produto" },
            quantidade = quantidade,
            precoCusto = precoCusto
        )

        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.insertProduto(produto)
                android.util.Log.d("EstoqueViewModel", "Produto salvo no Room: $produto")
                
                // Enviar ao Firestore
                try {
                    val produtoData = mapOf(
                        "nome" to produto.nome,
                        "quantidade" to produto.quantidade,
                        "precoCusto" to produto.precoCusto
                    )
                    val result = remoteRepository.uploadProdutoRemote(produtoData)
                    if (result.isSuccess) {
                        android.util.Log.d("EstoqueViewModel", "✅ Produto enviado ao Firestore!")
                    } else {
                        android.util.Log.e("EstoqueViewModel", "❌ Erro ao enviar produto: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EstoqueViewModel", "❌ Exceção ao enviar produto: ${e.message}")
                }
                
                // limpar campos
                novoNome = ""
                novaQuantidade = "0"
                novoPrecoCusto = ""
            } catch (e: Exception) {
                error = e.message ?: "Erro ao inserir produto."
                android.util.Log.e("EstoqueViewModel", "Erro ao inserir produto: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun updateProduto(produto: Produto) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.updateProduto(produto)
                android.util.Log.d("EstoqueViewModel", "Produto atualizado no Room: $produto")
                
                // Enviar ao Firestore
                try {
                    val produtoData = mapOf(
                        "id" to produto.id,
                        "nome" to produto.nome,
                        "quantidade" to produto.quantidade,
                        "precoCusto" to produto.precoCusto
                    )
                    val result = remoteRepository.uploadProdutoRemote(produtoData)
                    if (result.isSuccess) {
                        android.util.Log.d("EstoqueViewModel", "✅ Produto atualizado no Firestore!")
                    } else {
                        android.util.Log.e("EstoqueViewModel", "❌ Erro ao atualizar produto: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EstoqueViewModel", "❌ Exceção ao atualizar produto: ${e.message}")
                }
            } catch (e: Exception) {
                error = e.message ?: "Erro ao atualizar produto."
                android.util.Log.e("EstoqueViewModel", "Erro ao atualizar produto: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun deleteProduto(produto: Produto) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.deleteProduto(produto)
                android.util.Log.d("EstoqueViewModel", "✅ Produto deletado do Room: ${produto.nome}")
            } catch (e: Exception) {
                error = e.message ?: "Erro ao deletar produto."
                android.util.Log.e("EstoqueViewModel", "Erro ao deletar produto: ${e.message}")
            } finally {
                loading = false
            }
        }
    }
}