package com.grupo7.trabalhofinal.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bombas")
data class Bomba(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val identificador: String,
    val tipoCombustivel: String,
    val preco: Double,
    val status: String, // "ativa", "manutencao", etc.
    val produtoId: Long? = null // ID do produto no estoque
)