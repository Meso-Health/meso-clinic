package org.watsi.uhp

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.watsi.uhp.di.DaggerAppComponent
import org.watsi.uhp.managers.LocaleManager
import java.io.IOException
import javax.inject.Inject

class BaseApplication : DaggerApplication() {
    @Inject lateinit var localeManager: LocaleManager

    override fun onCreate() {
        super.onCreate()

        // The following deals with catching and handling errors
        // where the original completable has been removed or interrupted
        // meaning the connected error handler couldn't run as expected
        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            // RxJava wraps such errors with the UndeliverableException
            // but we want to process the wrapped exception
            if (e is UndeliverableException) {
                error = e.cause
            }

            if (error is InterruptedException || error is IOException) {
                return@setErrorHandler
            }

            if (error is NullPointerException || error is IllegalArgumentException) {
                propagate(e)
                return@setErrorHandler
            }

            if (error is IllegalStateException) {
                propagate(e)
                return@setErrorHandler
            }
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    private fun propagate(throwable: Throwable) {
        Thread.currentThread().uncaughtExceptionHandler.uncaughtException(
            Thread.currentThread(),
            throwable
        )
    }
}
