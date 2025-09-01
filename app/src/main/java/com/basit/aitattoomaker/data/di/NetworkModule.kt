package com.basit.aitattoomaker.data.di


import android.util.Log
import com.basit.aitattoomaker.BuildConfig
import com.basit.aitattoomaker.data.repo.TattooAI
import com.basit.aitattoomaker.data.repo.TattooApiService
import com.basit.aitattoomaker.data.responses.Tattoo
import com.basit.aitattoomaker.presentation.application.AppController.Companion.context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {



    @Singleton
    @Provides
    fun providesChuckInterceptor(): ChuckerInterceptor? {
        // Create the Collector
        val chuckerCollector = context?.let {
            ChuckerCollector(
                context = it,
                // Toggles visibility of the push notification
                showNotification = true,
                // Allows to customize the retention period of collected data
                retentionPeriod = RetentionManager.Period.ONE_HOUR
            )
        }

        // Create the Interceptor
        return chuckerCollector?.let {
            context?.applicationContext?.let { it1 ->
                ChuckerInterceptor.Builder(it1)
                    // The previously created Collector
                    .collector(it)
                    .maxContentLength(250_000L)
                    .alwaysReadResponseBody(true)
                    .build()
            }
        }
    }


    @Singleton
    @Provides
    fun provideOkHttp(
//        authenticationInterceptor: AuthenticationInterceptor
        chuckerInterceptor: ChuckerInterceptor?,
    ): OkHttpClient {
        val httpClient = OkHttpClient.Builder()

        httpClient.hostnameVerifier { _, _ -> true }

        if (BuildConfig.DEBUG){
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.addInterceptor(logging)
            Log.e("chucker", "Running")
            if (chuckerInterceptor != null) {
            httpClient.addInterceptor(chuckerInterceptor)
            }
        }
//        httpClient.addInterceptor(authenticationInterceptor)

        httpClient.writeTimeout(2, TimeUnit.MINUTES)
        httpClient.readTimeout(5, TimeUnit.MINUTES)
        httpClient.connectTimeout(5, TimeUnit.MINUTES)

        httpClient.retryOnConnectionFailure(true)  // Enable retries

        return httpClient.build()
    }

    @Singleton
    @Provides
    @Tattoo
    fun provideTattooRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder().client(okHttpClient)
            .baseUrl("https://65.1.178.163/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    @Singleton
    @Provides
    @TattooAI
    fun provideTattooAIRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder().client(okHttpClient)
            .baseUrl("https://65.1.178.163/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }


    @Singleton
    @Provides
    fun provideTattooService(@Tattoo retrofit: Retrofit): TattooApiService {
        return retrofit.create(TattooApiService::class.java)
    }

}