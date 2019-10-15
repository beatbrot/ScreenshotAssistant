package de.beatbrot.screenshotassistant.util

import android.view.animation.Animation

interface OpenAnimationListener : Animation.AnimationListener {
    override fun onAnimationEnd(animation: Animation?) {
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onAnimationStart(animation: Animation?) {
    }
}
