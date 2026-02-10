package com.example.roundupapp.di

import android.content.Context
import com.example.roundupapp.data.connectivity.AndroidNetworkConnectivityChecker
import com.example.roundupapp.domain.connectivity.NetworkConnectivityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing network connectivity dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule {

  @Provides
  @Singleton
  fun provideNetworkConnectivityChecker(
    @ApplicationContext context: Context
  ): NetworkConnectivityChecker {
    return AndroidNetworkConnectivityChecker(context)
  }
}
