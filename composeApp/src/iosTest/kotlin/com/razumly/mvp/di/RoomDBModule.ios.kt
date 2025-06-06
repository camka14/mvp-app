package com.razumly.mvp.di

import androidx.room.Room
import com.razumly.mvp.core.data.MVPDatabase
import com.razumly.mvp.core.data.getDatabase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

actual val roomDBModule = module {
    single {
        Napier.d(tag = "Database") { "Initializing Room database module" }
        try {
            getDatabase().build().also { db ->
                Napier.d(tag = "Database") { "Room database successfully initialized" }
            }
        } catch (e: Exception) {
            Napier.e(tag = "Database", throwable = e) { "Failed to initialize Room database" }
            throw e
        }
    }
}