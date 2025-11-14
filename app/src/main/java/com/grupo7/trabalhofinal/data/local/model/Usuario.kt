package com.grupo7.trabalhofinal.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String, // Firebase UID
    val nome: String,
    val email: String,
    val role: String // "admin", "funcionario", etc.
)
