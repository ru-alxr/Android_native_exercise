package ru.alxr.moviedatabasedemo.repository.config

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val RAW_CREDENTIALS = "themoviedb_api_key"

class ConfigRepository(
    private val config: FirebaseRemoteConfig,
    private val seconds: Long
) : IConfigRepository {

    override fun getServiceCredentials(): Single<String> {
        return implString(RAW_CREDENTIALS)
    }

    private fun implString(key: String): Single<String> {
        val executor = Executors.newSingleThreadExecutor()
        return Single
            .create { emitter ->
                config
                    .fetch(seconds)
                    .addOnSuccessListener(executor, OnSuccessListener<Void> {
                        emitValue(
                            key,
                            this@ConfigRepository::extractString,
                            emitter,
                            executor
                        )
                    })
                    .addOnFailureListener(executor, OnFailureListener { e ->
                        onFailure(emitter, e)
                    })
            }
    }

    private fun extractString(value: FirebaseRemoteConfigValue): String {
        return value.asString()
    }

    private fun <T> emitValue(
        key: String,
        extractor: (FirebaseRemoteConfigValue) -> T, emitter: SingleEmitter<T>,
        executor: ExecutorService
    ) {
        if (emitter.isDisposed) return
        config
            .activate()
            .addOnSuccessListener(executor, OnSuccessListener {
                if (emitter.isDisposed) return@OnSuccessListener
                val value = config.getValue(key)
                val extracted = extractor.invoke(value)
                emitter.onSuccess(extracted)
            })
            .addOnFailureListener(executor, OnFailureListener {
                if (emitter.isDisposed) return@OnFailureListener
                emitter.onError(it)
            })
    }

    private fun <T> onFailure(emitter: SingleEmitter<T>, e: Throwable) {
        if (emitter.isDisposed) return
        emitter.onError(e)
    }

}