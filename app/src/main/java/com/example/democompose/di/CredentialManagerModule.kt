package com.example.democompose.di

import com.example.democompose.manager.CredentialManager
import com.example.democompose.manager.CredentialManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// BINDS APPROACH
@Module
@InstallIn(SingletonComponent::class)
abstract class CredentialManagerModuleBinds {

    @Binds
    @Singleton
    abstract fun bindCredentialManager(impl: CredentialManagerImpl): CredentialManager
}

// PROVIDES APPROACH

//@Module
//@InstallIn(SingletonComponent::class)
//object CredentialManagerModuleProvides {
//
//    @Provides
//    @Singleton
//    fun provideCredentialManager(encryptedData: EncryptedData): CredentialManager {
//        return  CredentialManagerImpl(encryptedData)
//    }
//}