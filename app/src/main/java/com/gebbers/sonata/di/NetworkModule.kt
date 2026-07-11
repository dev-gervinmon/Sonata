package com.gebbers.sonata.di

import com.gebbers.sonata.data.remote.ITunesSearchService
import com.gebbers.sonata.data.remote.LyricsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("iTunes")
    fun provideITunesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("lyrics")
    fun provideLyricsRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.lyrics.ovh/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideITunesSearchService(@Named("iTunes") retrofit: Retrofit): ITunesSearchService {
        return retrofit.create(ITunesSearchService::class.java)
    }

    @Provides
    @Singleton
    fun provideLyricsService(@Named("lyrics") retrofit: Retrofit): LyricsService {
        return retrofit.create(LyricsService::class.java)
    }
}
