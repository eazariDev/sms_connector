package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.remote.AuthApiService
import com.example.data.remote.EventApiService
import com.example.data.remote.model.RefreshTokenRequest
import com.example.repository.EventRepository
import com.example.core.connector.sms.SmsConnector
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    val dataStoreManager: DataStoreManager by lazy {
        DataStoreManager(context)
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "sms_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    // Standalone Auth Client without Token Interceptor to prevent loops
    private val authOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder URL
            .client(authOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApiService::class.java)
    }

    private val tokenAuthenticator = object : Authenticator {
        private fun getResponseCount(response: Response): Int {
            var result = 1
            var prior = response.priorResponse
            while (prior != null) {
                result++
                prior = prior.priorResponse
            }
            return result
        }

        override fun authenticate(route: Route?, response: Response): Request? {
            // Check if we already retried (prevent infinite loop)
            if (getResponseCount(response) > 2) return null

            synchronized(this) {
                return runBlocking {
                    val currentRefreshToken = dataStoreManager.getRefreshToken()
                    val deviceId = dataStoreManager.getDeviceId()

                    if (currentRefreshToken.isNullOrEmpty()) {
                        dataStoreManager.clearTokens()
                        return@runBlocking null
                    }

                    try {
                        val authResponse = authApiService.refreshToken(
                            RefreshTokenRequest(deviceId, currentRefreshToken)
                        )
                        dataStoreManager.setTokens(authResponse.accessToken, authResponse.refreshToken)
                        
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.accessToken}")
                            .build()
                    } catch (e: Exception) {
                        dataStoreManager.clearTokens()
                        null
                    }
                }
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { dataStoreManager.getJwtToken() }
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val eventApiService: EventApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder URL
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(EventApiService::class.java)
    }

    val eventRepository: EventRepository by lazy {
        EventRepository(appDatabase.eventDao, eventApiService, dataStoreManager)
    }
    
    val notificationManager: com.example.core.notification.AppNotificationManager by lazy {
        com.example.core.notification.AppNotificationManager(context)
    }
    
    val smsConnector: SmsConnector by lazy {
        SmsConnector(eventRepository, dataStoreManager, notificationManager)
    }
}
