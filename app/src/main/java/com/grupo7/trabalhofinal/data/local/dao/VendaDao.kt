package com.grupo7.trabalhofinal.data.local.dao

import androidx.room.*
import com.grupo7.trabalhofinal.data.local.model.Venda
import kotlinx.coroutines.flow.Flow

@Dao
interface VendaDao {

    @Query("SELECT * FROM vendas ORDER BY data DESC")
    fun getAll(): Flow<List<Venda>>

    // vendas que ainda não foram sincronizadas com o Firebase
    @Query("SELECT * FROM vendas WHERE synced = 0")
    fun getUnsynced(): Flow<List<Venda>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(venda: Venda): Long

    @Update
    suspend fun update(venda: Venda)

    @Delete
    suspend fun delete(venda: Venda)

    // marca uma venda local como sincronizada
    @Query("UPDATE vendas SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
    
    // buscar vendas por usuarioId
    @Query("SELECT * FROM vendas WHERE usuarioId = :usuarioId ORDER BY data DESC")
    suspend fun getVendasByUsuarioId(usuarioId: String): List<Venda>
}