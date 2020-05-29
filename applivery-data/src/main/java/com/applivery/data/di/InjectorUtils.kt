/*
 * Copyright (c) 2020 Applivery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.applivery.data.di

import com.applivery.base.domain.AppliveryIdManager
import com.applivery.base.domain.SessionManager
import com.applivery.data.BuildConfig
import com.applivery.data.interceptor.HeadersInterceptor
import com.applivery.data.interceptor.SessionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal object InjectorUtils {

    fun provideOkHttpClient(): OkHttpClient {

        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder.interceptors().add(provideHeadersInterceptor())
        okHttpClientBuilder.interceptors().add(provideSessionInterceptor())

        if (BuildConfig.DEBUG) {
            okHttpClientBuilder.interceptors().add(provideHttpLoggingInterceptor())
        }

        return okHttpClientBuilder.build()
    }

    private fun provideHeadersInterceptor(): HeadersInterceptor {
        return HeadersInterceptor()
    }

    private fun provideSessionInterceptor(): SessionInterceptor {
        return SessionInterceptor(
            SessionManager.getInstance(),
            AppliveryIdManager.getInstance()
        )
    }

    private fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return loggingInterceptor
    }
}