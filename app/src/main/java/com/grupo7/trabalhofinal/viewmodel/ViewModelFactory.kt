package com.grupo7.trabalhofinal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository

/**
 * Fábrica simples para criar os ViewModels com os repositórios.
 * Segue o padrão didático do professor — sem usar Hilt/DI pesada.
 */
class ViewModelFactory(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(remoteRepository, localRepository, context) as T
            }
            modelClass.isAssignableFrom(BombasViewModel::class.java) -> {
                BombasViewModel(localRepository, remoteRepository) as T
            }
            modelClass.isAssignableFrom(VendasViewModel::class.java) -> {
                VendasViewModel(localRepository, remoteRepository) as T
            }
            modelClass.isAssignableFrom(EstoqueViewModel::class.java) -> {
                EstoqueViewModel(localRepository, remoteRepository) as T
            }
            modelClass.isAssignableFrom(RelatoriosViewModel::class.java) -> {
                RelatoriosViewModel(localRepository, remoteRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
