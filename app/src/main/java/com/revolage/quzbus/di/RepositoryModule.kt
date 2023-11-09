package com.revolage.quzbus.di

import com.revolage.quzbus.data.remote.QazBusApi
import com.revolage.quzbus.data.repositoryImpl.*
import com.revolage.quzbus.data.sharedpref.AppSharedPreferences
import com.revolage.quzbus.domain.repository.*
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
    fun provideIconRepository(
        sharedPreferences: AppSharedPreferences
    ) : IconRepository {
        return IconRepositoryImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideFavoriteRouteRepository(
        sharedPreferences: AppSharedPreferences
    ) : FavoriteRouteRepository {
        return FavoriteRouteRepositoryImpl(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideResetUserDataRepository(
        sharedPreferences: AppSharedPreferences
    ): ResetUserDataRepository {
        return ResetUserDataRepositoryImpl(sharedPreferences)
    }

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