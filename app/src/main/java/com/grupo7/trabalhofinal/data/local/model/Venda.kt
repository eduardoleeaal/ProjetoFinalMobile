package com.grupo7.trabalhofinal.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendas")
data class Venda(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bombaId: Long?,
    val usuarioId: String?,
    val litros: Double,
    val valor: Double,
    val pagamento: String,
    val data: Long,
    val synced: Boolean = false // para controlar sincronização
)