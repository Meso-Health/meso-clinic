package org.watsi.uhp.managers

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.watsi.device.managers.Logger

class NavigationManager(
        private val fragmentManager: FragmentManager,
        @IdRes private val fragmentContainer: Int,
        private val logger: Logger) {

    /**
     * Navigate to the supplied Fragment and add the FragmentTransaction to the back-stack
     */
    fun goTo(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        if (fragmentManager.backStackEntryCount == 0) {
            transaction.add(fragmentContainer, fragment)
        } else {
            transaction.replace(fragmentContainer, fragment)
        }
        transaction.addToBackStack(parseFragmentName(fragment)).commit()
    }

    /**
     * Navigate to the supplied fragment and pop all back-stack entries between the last instance
     * of the supplied fragment
     *
     * Use this when you do not want to allow the user to go back through previous screens
     */
    fun popTo(fragment: Fragment) {
        fragmentManager.popBackStack(parseFragmentName(fragment), FragmentManager.POP_BACK_STACK_INCLUSIVE)
        goTo(fragment)
    }

    /**
     * Used for going one screen back
     *
     * If a Fragment wants to define custom back behavior (such as showing a confirmation dialog)
     * it should implement the HandleOnBack interface
     */
    fun goBack() {
        // execute any pending transactions to ensure we are popping the most recent transaction
        fragmentManager.executePendingTransactions()

        // TODO: implement any other back functionality such as closing keyboard
        // ref: https://medium.com/@munnsthoughts/detecting-if-the-android-keyboard-is-open-using-kotlin-rxjava-2-8aee9fae262c
        if (fragmentManager.fragments.isEmpty() || fragmentManager.backStackEntryCount <= 1) {
            return
        }

        val currentFragment = fragmentManager.fragments.last()
        if (currentFragment is HandleOnBack) {
            currentFragment.onBack().subscribe(OnBackObserver())
        } else {
            fragmentManager.popBackStack()
        }
    }

    /**
     * A simple scheme for Fragment-specific tagging when adding to the backstack
     */
    private fun parseFragmentName(fragment: Fragment): String {
        return fragment.javaClass.simpleName
    }

    interface HandleOnBack {
        /**
         * For implementing custom onBack behavior
         *
         * @return Returns a Single that emits true if the NavigationManager
         * should proceed with the onBack
         */
        fun onBack(): Single<Boolean>
    }

    inner class OnBackObserver : SingleObserver<Boolean> {
        override fun onSuccess(goBack: Boolean) {
            if (goBack) {
                fragmentManager.popBackStack()
            }
        }

        override fun onSubscribe(d: Disposable) {
            // no-op
        }

        override fun onError(e: Throwable) {
            logger.error(e)
        }
    }
}
