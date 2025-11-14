package com.grupo7.trabalhofinal.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grupo7.trabalhofinal.data.local.dao.BombaDao
import com.grupo7.trabalhofinal.data.local.dao.ProdutoDao
import com.grupo7.trabalhofinal.data.local.dao.VendaDao
import com.grupo7.trabalhofinal.data.local.dao.UsuarioDao
import com.grupo7.trabalhofinal.data.local.model.Bomba
import com.grupo7.trabalhofinal.data.local.model.Produto
import com.grupo7.trabalhofinal.data.local.model.Venda
import com.grupo7.trabalhofinal.data.local.model.Usuario

@Database(
    entities = [Usuario::class, Bomba::class, Produto::class, Venda::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun bombaDao(): BombaDao
    abstract fun produtoDao(): ProdutoDao
    abstract fun vendaDao(): VendaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null



        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "posto_db"
            )
                .fallbackToDestructiveMigration() // Facilita desenvolvimento - remove em produção
                .build()
        }
    }
}