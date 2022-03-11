package com.fd.currencyconverter.di

import com.fd.currencyconverter.data.remote.CurrencyApi
import com.fd.currencyconverter.data.repository.CurrencyRepositoryImpl
import com.fd.currencyconverter.data.repository.RemoteDataSource
import com.fd.currencyconverter.domain.common.Constants
import com.fd.currencyconverter.domain.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object CurrencyModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()


    @Provides
    @Singleton
    fun provideRetrofitInstance(okHttpClient: OkHttpClient): CurrencyApi =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CurrencyApi::class.java)


    @Provides
    @Singleton
    fun provideRepository(remoteDataSource: RemoteDataSource): CurrencyRepository {
        return CurrencyRepositoryImpl(remoteDataSource)
    }
}