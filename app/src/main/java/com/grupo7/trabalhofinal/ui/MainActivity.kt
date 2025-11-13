package com.grupo7.trabalhofinal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.grupo7.trabalhofinal.data.local.db.AppDatabase
import com.grupo7.trabalhofinal.data.local.repository.LocalRepository
import com.grupo7.trabalhofinal.data.remote.RemoteRepository
import com.grupo7.trabalhofinal.data.sync.SyncManager
import com.grupo7.trabalhofinal.navigation.AppNavGraph
import com.grupo7.trabalhofinal.navigation.Screen
import com.grupo7.trabalhofinal.viewmodel.*
import com.grupo7.trabalhofinal.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Note: Using ViewModelProvider below inside setContent to pass factory-based VMs to Compose.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // instanciar DB e repositórios (padrão didático do professor)
        val db = AppDatabase.getInstance(applicationContext)
        val localRepository = LocalRepository(db)
        val remoteRepository = RemoteRepository()
        val syncManager = SyncManager(localRepository, remoteRepository)

        // ViewModelFactory
        val factory = ViewModelFactory(localRepository, remoteRepository)

        setContent {
            val navController = rememberNavController()

            // obter ViewModels usando ViewModelProvider com a factory (liga ao lifecycle da Activity)
            val viewModelProvider = remember { ViewModelProvider(this, factory) }

            val authViewModel: AuthViewModel = remember {
                viewModelProvider.get(AuthViewModel::class.java)
            }
            val bombasViewModel: BombasViewModel = remember {
                viewModelProvider.get(BombasViewModel::class.java)
            }
            val vendasViewModel: VendasViewModel = remember {
                viewModelProvider.get(VendasViewModel::class.java)
            }
            val estoqueViewModel: EstoqueViewModel = remember {
                viewModelProvider.get(EstoqueViewModel::class.java)
            }

            // Observa o usuário logado e inicia SyncManager quando houver login
            val currentUser by remember { derivedStateOf { authViewModel.user } }

            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    // iniciar sincronização no lifecycleScope da Activity (pode usar viewModelScope se preferir)
                    lifecycleScope.launch { syncManager.startSync(lifecycleScope) }
                } else {
                    // se quiser, pode parar a sincronização quando deslogar:
                    // lifecycleScope.launch { syncManager.stopSync() }
                }
            }

            // NavGraph com todos os ViewModels injetados
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                bombasViewModel = bombasViewModel,
                vendasViewModel = vendasViewModel,
                estoqueViewModel = estoqueViewModel
            )
        }
    }
}
