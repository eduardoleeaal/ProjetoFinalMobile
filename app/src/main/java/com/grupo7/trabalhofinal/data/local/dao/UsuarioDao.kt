package com.grupo7.trabalhofinal.data.local.dao

import androidx.room.*
import com.grupo7.trabalhofinal.data.local.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios")
    fun getAll(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario): Long

    @Update
    suspend fun update(usuario: Usuario)

    @Delete
    suspend fun delete(usuario: Usuario)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteById(id: String)
}
