package com.example.roundupapp.di

import com.example.roundupapp.data.repository.RoundUpRepositoryImpl
import com.example.roundupapp.domain.repository.RoundUpRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
//  @Provides
//  @Singleton
//  fun provideRoundUpRepository(): RoundUpApiService {
//    return Retrofit.Builder()
//      .baseUrl(Constants.BASE_URL)
//      .build()
//      .create(RoundUpApiService::class.java)
//  }
  @Provides
  @Singleton
  fun provideRoundUpRepository(): RoundUpRepository {
    return RoundUpRepositoryImpl()
  }
}
