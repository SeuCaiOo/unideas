package com.seucaio.unideas

import android.app.Application
import com.seucaio.unideas.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class UnideasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@UnideasApplication)
            modules(appModule)
        }
    }
}
