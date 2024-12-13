package com.razumly.mvp.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

actual class KoinInitializer(
    private val context: Context
) {
    actual fun init() {
        GlobalContext.startKoin {
            androidContext(context)
            modules(
                mainViewModelModule,
                MVPRepositoryModule,
                permissionsControllerModule,
                locationTrackerModule,
                eventSearchViewModelModule,
                tournamentContentViewModelModule,
                matchContentViewModelModule,
                createEventViewModelModule,
                clientModule,
                roomDBModule
            )
        }

    }
}