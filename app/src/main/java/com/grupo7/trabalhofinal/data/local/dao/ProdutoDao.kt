package com.grupo7.trabalhofinal.data.local.dao

import androidx.room.*
import com.grupo7.trabalhofinal.data.local.model.Produto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {

    @Query("SELECT * FROM produtos")
    fun getAll(): Flow<List<Produto>>

    @Query("SELECT * FROM produtos WHERE id = :id")
    suspend fun getById(id: Long): Produto?

    @Query("SELECT * FROM produtos WHERE nome LIKE '%' || :nome || '%'")
    suspend fun getByNome(nome: String): List<Produto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(produto: Produto): Long

    @Update
    suspend fun update(produto: Produto)

    @Delete
    suspend fun delete(produto: Produto)
}