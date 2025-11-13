package com.grupo7.trabalhofinal.data.sync

import android.util.Log
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class SyncManager(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) {

    private var job: Job? = null
    private val tag = "SyncManager"

    fun startSync(scope: CoroutineScope) {
        // se já estiver rodando, ignora
        if (job?.isActive == true) return

        job = scope.launch(coroutineContext) {
            // coletar continuamente; o Flow emitirá quando houver mudanças
            try {
                localRepository.getVendasUnsynced().collect { unsyncedList ->
                    if (unsyncedList.isEmpty()) {
                        // pequena pausa para evitar loop apertado quando não há nada
                        delay(1000)
                    } else {
                        unsyncedList.forEach { venda ->
                            if (!isActive) return@forEach
                            tryUploadAndMark(venda)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro no collector de sincronização: ${e.message}", e)
            }
        }
    }


    private suspend fun tryUploadAndMark(venda: Venda) = withContext(Dispatchers.IO) {
        val maxRetries = 2
        var attempt = 0
        var uploaded = false
        while (attempt <= maxRetries && !uploaded) {
            attempt++
            try {
                val result = remoteRepository.uploadVenda(venda)
                if (result.isSuccess) {
                    // marca localmente como sincronizado
                    // o id local de Venda pode ser 0-based; usamos venda.id
                    localRepository.markVendaSynced(venda.id)
                    Log.i(tag, "Venda ${venda.id} sincronizada (docId=${result.getOrNull()})")
                    uploaded = true
                } else {
                    val ex = result.exceptionOrNull()
                    Log.w(tag, "Falha ao subir venda ${venda.id} (tentativa $attempt): ${ex?.message}")
                    if (attempt <= maxRetries) delay(1_000L * attempt) // backoff linear simples
                }
            } catch (e: Exception) {
                Log.w(tag, "Exceção ao subir venda ${venda.id} (tentativa $attempt): ${e.message}")
                if (attempt <= maxRetries) delay(1_000L * attempt)
            }
        }

        if (!uploaded) {
            Log.e(tag, "Não foi possível sincronizar venda ${venda.id} após $maxRetries tentativas.")
            // Nota: manterá a venda com synced = false para futuras tentativas.
        }
    }


    suspend fun stopSync() {
        job?.cancelAndJoin()
        job = null
    }
}