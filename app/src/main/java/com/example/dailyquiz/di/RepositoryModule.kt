package com.example.dailyquiz.di

import com.example.dailyquiz.data.repository.MainRepository
import com.example.dailyquiz.data.repository.MainRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMainRepository(
        impl: MainRepositoryImpl
    ): MainRepository
}