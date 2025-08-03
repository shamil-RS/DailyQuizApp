package com.example.dailyquiz.di

import android.util.Log
import com.example.dailyquiz.data.remote.ApiService
import com.example.dailyquiz.data.remote.DailyQuizApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val logger = LoggerFactory.getLogger("OkHttpClient")

    @Provides
    @Named("header")
    fun provideHeaderInterceptor(): Interceptor = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .header("X-Debug", "true")
            .build()
        logger.debug("Header Interceptor added X-Debug")
        Log.d("Header", "Header Interceptor added X-Debug")
        chain.proceed(newRequest)
    }

    @Provides
    @Named("retry")
    fun provideRetryInterceptor(): Interceptor = Interceptor { chain ->
        var response = chain.proceed(chain.request())
        var tryCount = 0
        val maxRetry = 2

        while (!response.isSuccessful && tryCount < maxRetry) {
            Log.d("Retries", "Retry Interceptor: retry #${tryCount + 1}")
            tryCount++
            response.close()
            response = chain.proceed(chain.request())
        }
        response
    }

    @Provides
    @Named("logging")
    fun provideLoggingInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        logger.debug("Sending request: {} {}", request.method, request.url)
        Log.d("Logging", "Sending request: ${request.method} ${request.url}")

        val response = chain.proceed(request)

        logger.debug("Received response for {} with status {}", response.request.url, response.code)
        Log.d(
            "Logger",
            "Received response for ${response.request.url} with status ${response.code}"
        )

        response
    }

    @Provides
    @Named("network")
    fun provideNetworkInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        logger.debug("Network Interceptor - Sending request to URL: {}", request.url)
        Log.d("Network", "Network Interceptor - Sending request to URL: ${request.url}")

        val response = chain.proceed(request)

        logger.debug("Network Interceptor - Received response with status code: ${response.code}")
        Log.d(
            "Network",
            "Network Interceptor - Received response with status code: ${response.code}"
        )

        response
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        @Named("logging") loggingInterceptor: Interceptor,
        @Named("network") networkInterceptor: Interceptor,
        @Named("header") headerInterceptor: Interceptor,
        @Named("retry") retryInterceptor: Interceptor
    ): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }

            addInterceptor(loggingInterceptor)
            addInterceptor(headerInterceptor)
            addInterceptor(retryInterceptor)
            addNetworkInterceptor(networkInterceptor)
        }

        install(ContentNegotiation) {
            json(
                json = Json {
                    prettyPrint = true           // Красивый вывод JSON (необязательно)
                    isLenient = true             // Позволяет более гибкий парсинг
                    ignoreUnknownKeys = true     // Игнорировать неизвестные поля в JSON
                    coerceInputValues = true    // Подставлять дефолтные значения при некорректных данных
                },
                contentType = ContentType.Application.Json
            )
        }
    }

    @Provides
    @Singleton
    fun provideApiService(httpClient: HttpClient): ApiService = DailyQuizApiClient(httpClient)
}