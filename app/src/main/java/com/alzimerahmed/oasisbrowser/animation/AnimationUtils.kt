package com.alzimerahmed.oasisbrowser.animation

import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import com.alzimerahmed.oasisbrowser.R

/**
 * Animation specific helper code.
 */
object AnimationUtils {

    /**
     * Creates an animation that rotates an [ImageView] around the Y axis by 180 degrees and changes
     * the image resource shown when the view is rotated 90 degrees to the user.
     *
     * @param imageView   the view to rotate.
     * @param drawableRes the drawable to set when the view is rotated by 90 degrees.
     * @return an animation that will change the image shown by the view.
     */
    @JvmStatic
    fun createRotationTransitionAnimation(
        context: Context,
        imageView: ImageView,
        @DrawableRes drawableRes: Int,
        reducedMotion: Boolean = false
    ): Animation = object : Animation() {

        private var setFinalDrawable: Boolean = false

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) =
            if (interpolatedTime < 0.5f) {
                imageView.rotationY = 90f * interpolatedTime * 2f
            } else {
                if (!setFinalDrawable) {
                    setFinalDrawable = true
                    imageView.setImageResource(drawableRes)
                }
                imageView.rotationY = -90 + 90f * (interpolatedTime - 0.5f) * 2f
            }

    }.apply {
        duration = getDuration(context, R.integer.motion_duration_emphasized, reducedMotion)
        interpolator = AccelerateDecelerateInterpolator()
    }

    /**
     * Resolves a motion duration in milliseconds. When [reducedMotion] is true the duration is
     * collapsed to zero so animations become instant.
     */
    @JvmStatic
    fun getDuration(
        context: Context,
        @IntegerRes durationRes: Int,
        reducedMotion: Boolean = false
    ): Long = if (reducedMotion) {
        0L
    } else {
        context.resources.getInteger(durationRes).toLong()
    }

}
