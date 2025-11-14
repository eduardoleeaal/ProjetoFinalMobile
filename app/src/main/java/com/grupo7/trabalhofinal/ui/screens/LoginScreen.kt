package com.grupo7.trabalhofinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.grupo7.trabalhofinal.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val user by remember { derivedStateOf { authViewModel.user } }
    val loading by remember { derivedStateOf { authViewModel.loading } }
    val error by remember { derivedStateOf { authViewModel.error } }

    // Observe user -> navigate when logged
    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Posto - Login", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = authViewModel.email,
            onValueChange = { authViewModel.email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = authViewModel.password,
            onValueChange = { authViewModel.password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { authViewModel.signIn() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Entrar")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { authViewModel.signUp() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar conta")
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}
