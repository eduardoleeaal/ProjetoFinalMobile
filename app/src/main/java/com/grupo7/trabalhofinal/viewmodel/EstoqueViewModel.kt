package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EstoqueViewModel(
    private val localRepository: LocalRepository
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
    var novoPrecoVenda by mutableStateOf("")

    fun addProduto() {
        val quantidade = novaQuantidade.toIntOrNull() ?: run {
            error = "Quantidade inválida"
            return
        }
        val precoCusto = novoPrecoCusto.toDoubleOrNull() ?: run {
            error = "Preço de custo inválido"
            return
        }
        val precoVenda = novoPrecoVenda.toDoubleOrNull() ?: run {
            error = "Preço de venda inválido"
            return
        }

        val produto = Produto(
            nome = novoNome.ifBlank { "Produto" },
            quantidade = quantidade,
            precoCusto = precoCusto,
            precoVenda = precoVenda
        )

        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.insertProduto(produto)
                // limpar campos
                novoNome = ""
                novaQuantidade = "0"
                novoPrecoCusto = ""
                novoPrecoVenda = ""
            } catch (e: Exception) {
                error = e.message ?: "Erro ao inserir produto."
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
            } catch (e: Exception) {
                error = e.message ?: "Erro ao atualizar produto."
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
            } catch (e: Exception) {
                error = e.message ?: "Erro ao deletar produto."
            } finally {
                loading = false
            }
        }
    }
}