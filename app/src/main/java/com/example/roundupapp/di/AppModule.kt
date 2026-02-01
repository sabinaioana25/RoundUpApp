package com.example.roundupapp.di

import android.content.Context
import com.example.roundupapp.data.database.RoundUpDatabase
import com.example.roundupapp.data.repository.RoundUpRepositoryImpl
import com.example.roundupapp.domain.repository.RoundUpRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext context: Context) : RoundUpDatabase {
    return RoundUpDatabase.getInstance(context)
  }

  @Provides
  @Singleton
  fun provideRoundUpRepository(database: RoundUpDatabase): RoundUpRepository {
    return RoundUpRepositoryImpl(database)
  }
}
