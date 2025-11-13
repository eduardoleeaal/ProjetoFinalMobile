package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo7.trabalhofinal.data.local.model.Bomba
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BombasViewModel(
    private val localRepository: LocalRepository
) : ViewModel() {

    // Lista de bombas exposta como State (via StateFlow)
    val bombas = localRepository.getBombas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Campos para formulário (exemplos)
    var novoIdentificador by mutableStateOf("")
    var novoTipo by mutableStateOf("")
    var novoPreco by mutableStateOf("")

    fun addBomba() {
        val preco = novoPreco.toDoubleOrNull() ?: run {
            error = "Preço inválido"
            return
        }

        val bomba = Bomba(
            identificador = novoIdentificador.ifBlank { "Bomba" },
            tipoCombustivel = novoTipo.ifBlank { "Gasolina" },
            preco = preco,
            status = "ativa"
        )

        viewModelScope.launch {
            loading = true
            error = null
            try {
                localRepository.insertBomba(bomba)
                // limpar campos
                novoIdentificador = ""
                novoTipo = ""
                novoPreco = ""
            } catch (e: Exception) {
                error = e.message ?: "Erro ao inserir bomba."
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
                localRepository.updateBomba(bomba)
            } catch (e: Exception) {
                error = e.message ?: "Erro ao atualizar bomba."
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
            } catch (e: Exception) {
                error = e.message ?: "Erro ao deletar bomba."
            } finally {
                loading = false
            }
        }
    }
}