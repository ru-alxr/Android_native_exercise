package ru.alxr.moviedatabasedemo.di

import android.view.LayoutInflater
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import okhttp3.*
import okio.Buffer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.alxr.moviedatabasedemo.BuildConfig
import ru.alxr.moviedatabasedemo.application.getAppLogger
import ru.alxr.moviedatabasedemo.db.AppDatabase
import ru.alxr.moviedatabasedemo.navigation.FeatureNavigation
import ru.alxr.moviedatabasedemo.navigation.IFeatureNavigation
import ru.alxr.moviedatabasedemo.repository.config.ConfigRepository
import ru.alxr.moviedatabasedemo.repository.config.IConfigRepository
import ru.alxr.moviedatabasedemo.repository.config.RAW_CREDENTIALS
import ru.alxr.moviedatabasedemo.utils.ILogger
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

val APPLICATION_MODULE = module {

    single { androidContext().getAppLogger() }

    single(createOnStart = true) { AppDatabase.getInstance(androidContext()) as AppDatabase }

    single { LayoutInflater.from(androidContext()) }

    single { getRemoteConfig() }

    single { ConfigRepository(config = get(), seconds = 60L) as IConfigRepository }

    single {
        FeatureNavigation() as IFeatureNavigation
    }

    single { provideOkHttpClient(get()) }

    single {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.MOVIES_DB_ENDPOINT)
            .client(get())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

}

private fun getRemoteConfig(): FirebaseRemoteConfig {
    val config = FirebaseRemoteConfig.getInstance()
    val settings = FirebaseRemoteConfigSettings
        .Builder()
        .setDeveloperModeEnabled(true)
        .build()
    config.setConfigSettings(settings)
    val defValues = HashMap<String, Any>()
    defValues[RAW_CREDENTIALS] = ""
    config.setDefaults(defValues)
    return config
}

private fun provideOkHttpClient(logger: ILogger): OkHttpClient {
    val okHttpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(ResponseCodeInterceptor(logger))
    return okHttpClientBuilder.build()
}

private class ResponseCodeInterceptor(val logger: ILogger) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val url = String.format(
            "%s://%s%s",
            original.url().scheme(),
            original.url().host(),
            original.url().encodedPath()
        )
        val method = original.method()
        val response: Response
        val responseBody: ResponseBody?
        response = chain.proceed(original)
        responseBody = response.body()
        val code = response.code()
        val body = getBody(original.body())
        responseBody!!
        val rawJson: String = responseBody.string()//.add(code)
        logger
            .with(this)
            .add("$method $url ${body ?: ""} ")
            .add("Response is code=$code raw=$rawJson")
            .log()
        return response
            .newBuilder()
            .body(ResponseBody.create(responseBody.contentType(), rawJson))
            .code(200)
            .build()
    }

    companion object {
        const val ENCODING = "UTF-8"
        val UTF8: Charset = Charset.forName(ENCODING)
    }

    private fun getBody(requestBody: RequestBody?): String? {
        if (requestBody == null) return null
        val buffer = Buffer()
        try {
            requestBody.writeTo(buffer)
        } catch (e: IOException) {
            return null
        }

        if (!isPlaintext(buffer)) return null
        return java.net.URLDecoder.decode(buffer.readString(UTF8), ENCODING)
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false
        }
    }

}