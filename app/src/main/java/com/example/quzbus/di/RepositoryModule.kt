package com.example.quzbus.di

import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.repositoryImpl.AuthRepositoryImpl
import com.example.quzbus.data.repositoryImpl.CitiesRepositoryImpl
import com.example.quzbus.data.repositoryImpl.SaveDataRepositoryImpl
import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.domain.repository.SaveDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSaveDataRepository(
        sharedPreferences: AppSharedPreferences
    ): SaveDataRepository {
        return SaveDataRepositoryImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideCitiesRepository(
        api: QazBusApi
    ): CitiesRepository {
        return CitiesRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        api: QazBusApi
    ): AuthRepository {
        return AuthRepositoryImpl(api)
    }
}