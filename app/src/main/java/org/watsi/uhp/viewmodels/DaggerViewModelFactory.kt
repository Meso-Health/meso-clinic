package org.watsi.uhp.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * A ViewModelProvider.Factory implementation that supports constructing/injecting ViewModels
 * using Dagger. The input map is provided by our ViewModelModule.
 *
 * ref: https://developer.android.com/reference/android/arch/lifecycle/ViewModelProvider.Factory.html
 */
@Singleton
class DaggerViewModelFactory @Inject constructor(
        private val viewModelProviderMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // if exact model class does not exist in the map, loop through entries and provide any
        //  model that is a subclass of the required class
        val provider = viewModelProviderMap[modelClass] ?: viewModelProviderMap.entries.find {
            modelClass.isAssignableFrom(it.key)
        }?.value

        // do a null check to make sure the method is null-safe while also providing an informative exception
        if (provider?.get() == null) {
            throw UnsupportedViewModelException("unknown model class " + modelClass)
        } else {
            return provider.get() as T
        }
    }

    class UnsupportedViewModelException(override var message: String) : java.lang.IllegalArgumentException(message)
}
