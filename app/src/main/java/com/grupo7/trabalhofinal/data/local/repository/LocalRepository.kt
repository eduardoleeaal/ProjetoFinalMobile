package com.grupo7.trabalhofinal.data.local.repository

import com.grupo7.trabalhofinal.data.local.db.AppDatabase
import com.grupo7.trabalhofinal.data.local.model.Bomba
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.data.local.model.Usuario
import kotlinx.coroutines.flow.Flow


class LocalRepository(
    private val db: AppDatabase
) {

    // DAOs
    private val usuarioDao = db.usuarioDao()
    private val bombaDao = db.bombaDao()
    private val produtoDao = db.produtoDao()
    private val vendaDao = db.vendaDao()

    // --- Usuarios ---
    fun getUsuarios(): Flow<List<Usuario>> = usuarioDao.getAll()

    suspend fun getUsuarioById(id: String): Usuario? = usuarioDao.getById(id)

    suspend fun insertUsuario(usuario: Usuario): Long = usuarioDao.insert(usuario)

    suspend fun updateUsuario(usuario: Usuario) = usuarioDao.update(usuario)

    suspend fun deleteUsuario(usuario: Usuario) = usuarioDao.delete(usuario)

    // --- Bombas ---
    fun getBombas(): Flow<List<Bomba>> = bombaDao.getAll()

    suspend fun insertBomba(bomba: Bomba): Long = bombaDao.insert(bomba)

    suspend fun updateBomba(bomba: Bomba) = bombaDao.update(bomba)

    suspend fun deleteBomba(bomba: Bomba) = bombaDao.delete(bomba)

    // --- Produtos (Estoque) ---
    fun getProdutos(): Flow<List<Produto>> = produtoDao.getAll()

    suspend fun insertProduto(produto: Produto): Long = produtoDao.insert(produto)

    suspend fun updateProduto(produto: Produto) = produtoDao.update(produto)

    suspend fun deleteProduto(produto: Produto) = produtoDao.delete(produto)

    // --- Vendas ---
    fun getVendas(): Flow<List<Venda>> = vendaDao.getAll()

    // Vendas não sincronizadas (synced = false)
    fun getVendasUnsynced(): Flow<List<Venda>> = vendaDao.getUnsynced()

    suspend fun insertVenda(venda: Venda): Long = vendaDao.insert(venda)

    suspend fun updateVenda(venda: Venda) = vendaDao.update(venda)

    suspend fun deleteVenda(venda: Venda) = vendaDao.delete(venda)

    suspend fun markVendaSynced(localId: Long) = vendaDao.markSynced(localId)
    
    suspend fun getVendasByUsuarioId(usuarioId: String): List<Venda> = vendaDao.getVendasByUsuarioId(usuarioId)
}