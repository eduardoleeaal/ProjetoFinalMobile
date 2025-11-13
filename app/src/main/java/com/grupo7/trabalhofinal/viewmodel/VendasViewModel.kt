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

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Campos do formulário de venda
    var selectedBombaId by mutableStateOf<Long?>(null)
    var litrosInput by mutableStateOf("")
    var pagamentoInput by mutableStateOf("Dinheiro")

    /**
     * Insere uma venda localmente (synced = false por padrão).
     * A sincronização ficará a cargo do SyncManager (ou podemos tentar subir imediatamente aqui).
     */
    fun registerVenda(usuarioId: String?) {
        val litros = litrosInput.toDoubleOrNull()
        if (litros == null) {
            error = "Litros inválidos"
            return
        }

        // calcular valor simplificado: teremos que buscar preço da bomba em outro lugar;
        // aqui exemplificamos com valor = litros * 1.0 (ajuste no app real)
        val valor = litros * 1.0

        val venda = Venda(
            bombaId = selectedBombaId,
            usuarioId = usuarioId,
            litros = litros,
            valor = valor,
            pagamento = pagamentoInput,
            data = System.currentTimeMillis(),
            synced = false
        )

        viewModelScope.launch {
            loading = true
            error = null
            try {
                val localId = localRepository.insertVenda(venda)
                // opcional: tentar subir imediatamente (modo didático)
                // tentaUploadImediato(localId)
            } catch (e: Exception) {
                error = e.message ?: "Erro ao registrar venda."
            } finally {
                loading = false
            }
        }
    }

    /**
     * Exemplo (opcional): tentar upload imediato de uma venda local recém criada.
     * Se for bem sucedido, marca como sincronizada.
     */
    private suspend fun tentaUploadImediato(localId: Long) {
        try {
            // obter venda local (não fornecemos DAO getById neste esqueleto; se precisar, adicione)
            // aqui vamos supor que você altere VendaDao para permitir buscar por id
            // val venda = localRepository.getVendaById(localId)
            // val res = remoteRepository.uploadVenda(venda)
            // if (res.isSuccess) localRepository.markVendaSynced(localId)
        } catch (e: Exception) {
            // ignora: SyncManager fará as tentativas
        }
    }
}