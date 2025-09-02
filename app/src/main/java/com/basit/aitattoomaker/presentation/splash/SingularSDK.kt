package com.basit.aitattoomaker.presentation.splash

import com.singular.sdk.SingularConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingularSDK {
    @Provides
    @Singleton
    fun provideSingularConfig(): SingularConfig {
        return SingularConfig(
            "terafort_new_94135a02",
            "387f4c401b63a8ba72835bdedeb6a91b"
        ).withLoggingEnabled().withLogLevel(1)
    }
}