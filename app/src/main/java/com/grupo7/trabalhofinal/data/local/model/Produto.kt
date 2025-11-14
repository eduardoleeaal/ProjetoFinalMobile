package com.grupo7.trabalhofinal.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nome: String,
    val quantidade: Int,
    val precoCusto: Double
)