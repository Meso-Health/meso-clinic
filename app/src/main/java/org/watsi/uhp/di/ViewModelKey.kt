package org.watsi.uhp.di

import android.arch.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)
