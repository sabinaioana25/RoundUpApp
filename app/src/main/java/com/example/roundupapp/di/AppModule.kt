package com.example.roundupapp.di

import com.example.roundupapp.data.repository.RoundUpRepositoryImpl
import com.example.roundupapp.domain.repository.RoundUpRepository
import com.example.roundupapp.domain.usecase.LoadInitialDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides
  @Singleton
  fun provideRoundUpRepository(): RoundUpRepository {
    return RoundUpRepositoryImpl()
  }
}
