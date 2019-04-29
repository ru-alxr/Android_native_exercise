package ru.alxr.moviedatabasedemo.repository.credential

import android.util.Base64
import io.reactivex.Single
import ru.alxr.moviedatabasedemo.repository.config.IConfigRepository
import ru.alxr.moviedatabasedemo.utils.ILogger
import java.lang.RuntimeException
import java.nio.charset.Charset

class CredentialRepository(private val config: IConfigRepository,
                           private val logger:ILogger) : ICredentialRepository {

    private lateinit var mCachedApiKey:String

    override fun getMovieDatabaseCredential(): Single<String> {
        if (::mCachedApiKey.isInitialized) return Single.just(mCachedApiKey)
        return config
            .getServiceCredentials()
            .map { decode(it)}
            .map { cache(it) }
    }

    private fun decode(source:String):String{
        if (source.isEmpty()) throw RuntimeException("No credential found")
        return String(Base64.decode(source, Base64.DEFAULT), Charset.forName("utf-8"))
    }

    private fun cache(value:String):String{
        mCachedApiKey = value
        logger.with(this).add("Api key is $value").log()
        return value
    }

}