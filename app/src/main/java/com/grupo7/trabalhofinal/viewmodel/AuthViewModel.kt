package com.grupo7.trabalhofinal.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.grupo7.trabalhofinal.data.local.model.Usuario
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository,
    private val context: Context
) : ViewModel() {

    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var user by mutableStateOf<FirebaseUser?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Verificar se há sessão salva ao iniciar
        checkSavedSession()
    }

    private fun checkSavedSession() {
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            // Verificar se o usuário do Firebase ainda está autenticado
            user = remoteRepository.getCurrentUser()
        }
    }

    private fun saveLoginState(loggedIn: Boolean) {
        sharedPrefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

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
                    saveLoginState(true)
                    android.util.Log.d("AuthViewModel", "✅ Login bem-sucedido! UID: ${user?.uid}")
                    // Salvar usuário no Room
                    user?.let { firebaseUser ->
                        val usuario = Usuario(
                            id = firebaseUser.uid,
                            nome = firebaseUser.displayName ?: "Usuário",
                            email = firebaseUser.email ?: email,
                            role = "funcionario" // Padrão, pode ser ajustado
                        )
                        localRepository.insertUsuario(usuario)
                    }
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
                    saveLoginState(true)
                    android.util.Log.d("AuthViewModel", "✅ Cadastro bem-sucedido! UID: ${user?.uid}")
                    // Salvar usuário no Room
                    user?.let { firebaseUser ->
                        val usuario = Usuario(
                            id = firebaseUser.uid,
                            nome = firebaseUser.displayName ?: "Usuário",
                            email = firebaseUser.email ?: email,
                            role = "funcionario" // Padrão, pode ser ajustado
                        )
                        localRepository.insertUsuario(usuario)
                    }
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
        saveLoginState(false)
        email = ""
        password = ""
    }
}