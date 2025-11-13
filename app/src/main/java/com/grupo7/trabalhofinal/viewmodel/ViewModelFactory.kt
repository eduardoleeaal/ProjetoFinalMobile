package com.grupo7.trabalhofinal.viewmodel

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
    private val remoteRepository: RemoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(remoteRepository) as T
            }
            modelClass.isAssignableFrom(BombasViewModel::class.java) -> {
                BombasViewModel(localRepository) as T
            }
            modelClass.isAssignableFrom(VendasViewModel::class.java) -> {
                VendasViewModel(localRepository, remoteRepository) as T
            }
            modelClass.isAssignableFrom(EstoqueViewModel::class.java) -> {
                EstoqueViewModel(localRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
