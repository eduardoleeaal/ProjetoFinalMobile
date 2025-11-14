package com.grupo7.trabalhofinal.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.grupo7.trabalhofinal.data.local.model.Venda
import kotlinx.coroutines.tasks.await


class RemoteRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- Auth ---
    fun currentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // --- Firestore: Vendas ---

    suspend fun uploadVenda(venda: Venda): Result<String> {
        try {
            val data = mapOf(
                "bombaId" to (venda.bombaId ?: -1L),
                "usuarioId" to (venda.usuarioId ?: ""),
                "litros" to venda.litros,
                "valor" to venda.valor,
                "pagamento" to venda.pagamento,
                "data" to venda.data
            )
            val docRef = firestore.collection("vendas").add(data).await()
            return Result.success(docRef.id)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun fetchRecentVendas(limit: Long = 100): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("vendas")
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val list = snapshot.documents.map { it.data ?: emptyMap<String, Any>() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Firestore: Usuarios ---
    suspend fun uploadUsuario(usuario: Map<String, Any>): Result<String> {
        return try {
            val ref = firestore.collection("usuarios").add(usuario).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Firestore: Bombas ---
    suspend fun uploadBombaRemote(bombaData: Map<String, Any>): Result<String> {
        return try {
            val ref = firestore.collection("bombas").add(bombaData).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProdutoRemote(produtoData: Map<String, Any>): Result<String> {
        return try {
            val ref = firestore.collection("produtos").add(produtoData).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun fetchCollection(collection: String, limit: Long = 100): Result<List<Pair<String, Map<String, Any>>>> {
        return try {
            val snapshot = firestore.collection(collection)
                .limit(limit)
                .get()
                .await()
            val mapped = snapshot.documents.map { it.id to (it.data ?: emptyMap()) }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Método para obter todas as vendas (para relatórios) ---
    suspend fun getVendas(): List<Venda> {
        return try {
            val snapshot = firestore.collection("vendas")
                .orderBy("data", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Venda(
                        id = 0L, // ID local não usado aqui
                        bombaId = (doc.getLong("bombaId") ?: -1L),
                        usuarioId = doc.getString("usuarioId") ?: "",
                        litros = doc.getDouble("litros") ?: 0.0,
                        valor = doc.getDouble("valor") ?: 0.0,
                        pagamento = doc.getString("pagamento") ?: "",
                        data = doc.getLong("data") ?: 0L,
                        synced = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}