package com.grupo7.trabalhofinal.data.local.dao

import androidx.room.*
import com.grupo7.trabalhofinal.data.local.model.Bomba
import kotlinx.coroutines.flow.Flow

@Dao
interface BombaDao {

    @Query("SELECT * FROM bombas")
    fun getAll(): Flow<List<Bomba>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bomba: Bomba): Long

    @Update
    suspend fun update(bomba: Bomba)

    @Delete
    suspend fun delete(bomba: Bomba)
}