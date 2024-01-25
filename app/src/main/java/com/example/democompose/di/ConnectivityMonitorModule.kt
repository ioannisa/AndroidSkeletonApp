package com.example.democompose.di

import android.content.Context
import com.example.democompose.network.monitor.ConnectivityMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ConnectivityMonitorModule {

    @Singleton
    @Provides
    fun provideConnectivityMonitor(@ApplicationContext context: Context): ConnectivityMonitor {
        return ConnectivityMonitor(context)
    }
}