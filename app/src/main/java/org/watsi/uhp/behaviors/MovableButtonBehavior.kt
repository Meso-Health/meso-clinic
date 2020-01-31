package org.watsi.uhp.behaviors

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.SnackbarLayout
import android.util.AttributeSet
import android.view.View
import android.widget.Button

// This file is taken from https://stackoverflow.com/questions/33709953/make-snackbar-push-view-upwards
class MovableButtonBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<Button>() {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: Button, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, button: Button, snackbarLayout: View): Boolean {
        val translationY = Math.min(0.0f, snackbarLayout.translationY - snackbarLayout.height)
        button.translationY = translationY
        return true
    }
}
