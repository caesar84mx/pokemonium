package com.mdymnoff.pokemonium

import android.app.Application
import com.mdymnoff.pokemonium.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PokemonApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@PokemonApplication)
            modules(appModule)
        }
    }
}
