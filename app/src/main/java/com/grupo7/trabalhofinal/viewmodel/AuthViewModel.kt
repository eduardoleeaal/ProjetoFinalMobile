package com.grupo7.trabalhofinal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var user by mutableStateOf<FirebaseUser?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /**
     * Tenta logar com email/senha. Atualiza 'user' em caso de sucesso.
     */
    fun signIn() {
        if (email.isBlank() || password.isBlank()) {
            error = "Email e senha são obrigatórios."
            return
        }

        viewModelScope.launch {
            loading = true
            error = null
            try {
                val res = remoteRepository.signIn(email.trim(), password)
                if (res.isSuccess) {
                    user = res.getOrNull()
                } else {
                    error = res.exceptionOrNull()?.message ?: "Erro desconhecido ao logar."
                }
            } catch (e: Exception) {
                error = e.message ?: "Erro inesperado."
            } finally {
                loading = false
            }
        }
    }

    fun signUp() {
        if (email.isBlank() || password.isBlank()) {
            error = "Email e senha são obrigatórios."
            return
        }

        viewModelScope.launch {
            loading = true
            error = null
            try {
                val res = remoteRepository.signUp(email.trim(), password)
                if (res.isSuccess) {
                    user = res.getOrNull()
                } else {
                    error = res.exceptionOrNull()?.message ?: "Erro desconhecido ao cadastrar."
                }
            } catch (e: Exception) {
                error = e.message ?: "Erro inesperado."
            } finally {
                loading = false
            }
        }
    }

    fun signOut() {
        remoteRepository.signOut()
        user = null
    }
}