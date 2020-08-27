package com.dicoding.tourismapp

import android.app.Application
import com.anggastudio.tourismapp.core.di.databaseModule
import com.anggastudio.tourismapp.core.di.networkModule
import com.anggastudio.tourismapp.core.di.repositoryModule
import com.dicoding.tourismapp.di.useCaseModule
import com.dicoding.tourismapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(
                listOf(
                    databaseModule, networkModule, repositoryModule, useCaseModule, viewModelModule
                )
            )
        }
    }
}