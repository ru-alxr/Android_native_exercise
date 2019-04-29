package ru.alxr.moviedatabasedemo.application

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.startKoin
import ru.alxr.moviedatabasedemo.R
import ru.alxr.moviedatabasedemo.di.APPLICATION_MODULE
import ru.alxr.moviedatabasedemo.di.DETAILS_VIEW_MODULE
import ru.alxr.moviedatabasedemo.di.MAIN_VIEW_MODULE
import ru.alxr.moviedatabasedemo.utils.AppLogger
import ru.alxr.moviedatabasedemo.utils.ILogger

class DemoApplication : Application() {

    private lateinit var mDebugLogger: ILogger

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        startKoin(
            this@DemoApplication,
            listOf(
                APPLICATION_MODULE,
                MAIN_VIEW_MODULE,
                DETAILS_VIEW_MODULE
            )
        )
        addUndeliverableErrorHandler()
    }

    private fun addUndeliverableErrorHandler() {
        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) return@setErrorHandler
            throw e as Exception
        }
    }

    fun getLogger(): ILogger {
        if (!::mDebugLogger.isInitialized) mDebugLogger = AppLogger(resources.getBoolean(R.bool.enableDebugLogging))
        return mDebugLogger
    }

}

fun Context.getAppLogger(): ILogger {
    return (applicationContext as DemoApplication).getLogger()
}