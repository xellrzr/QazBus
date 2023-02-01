package com.example.quzbus.di

import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.repositoryImpl.AuthRepositoryImpl
import com.example.quzbus.data.repositoryImpl.CitiesRepositoryImpl
import com.example.quzbus.data.repositoryImpl.RoutesRepositoryImpl
import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.domain.repository.RoutesRepository
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
    fun provideRoutesRepository(
        api: QazBusApi,
        sharedPreferences: AppSharedPreferences
    ): RoutesRepository {
        return RoutesRepositoryImpl(api,sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideCitiesRepository(
        api: QazBusApi,
        sharedPreferences: AppSharedPreferences
    ): CitiesRepository {
        return CitiesRepositoryImpl(api, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        api: QazBusApi,
        sharedPreferences: AppSharedPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(api, sharedPreferences)
    }
}