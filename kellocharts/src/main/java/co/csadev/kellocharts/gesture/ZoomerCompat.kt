/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.csadev.kellocharts.gesture

import android.os.SystemClock
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

/**
 * A simple class that animates double-touch zoom gestures. Functionally similar to a [android.widget.Scroller].
 */
class ZoomerCompat {
    /**
     * The interpolator, used for making zooms animate 'naturally.'
     */
    private val mInterpolator: Interpolator

    /**
     * The total animation duration for a zoom.
     */
    private val mAnimationDurationMillis: Long

    /**
     * Whether or not the current zoom has finished.
     */
    private var mFinished = true

    /**
     * The current zoom value; computed by [.computeZoom].
     */
    /**
     * Returns the current zoom level.
     *
     * @see android.widget.Scroller.getCurrX
     */
    var currZoom: Float = 0f
        private set

    /**
     * The time the zoom started, computed using [SystemClock.elapsedRealtime].
     */
    private var mStartRTC: Long = 0

    /**
     * The destination zoom factor.
     */
    private var mEndZoom: Float = 0f

    init {
        mInterpolator = DecelerateInterpolator()
        mAnimationDurationMillis = DEFAULT_SHORT_ANIMATION_DURATION
    }

    /**
     * Forces the zoom finished state to the given value. Unlike [.abortAnimation], the current zoom value isn't
     * set to the ending value.
     *
     * @see android.widget.Scroller.forceFinished
     */
    fun forceFinished(finished: Boolean) {
        mFinished = finished
    }

    /**
     * Aborts the animation, setting the current zoom value to the ending value.
     *
     * @see android.widget.Scroller.abortAnimation
     */
    fun abortAnimation() {
        mFinished = true
        currZoom = mEndZoom
    }

    /**
     * Starts a zoom from 1.0 to (1.0 + endZoom). That is, to zoom from 100% to 125%, endZoom should by 0.25f.
     *
     * @see android.widget.Scroller.startScroll
     */
    fun startZoom(endZoom: Float) {
        mStartRTC = SystemClock.elapsedRealtime()
        mEndZoom = endZoom

        mFinished = false
        currZoom = 1f
    }

    /**
     * Computes the current zoom level, returning true if the zoom is still active and false if the zoom has finished.
     *
     * @see android.widget.Scroller.computeScrollOffset
     */
    fun computeZoom(): Boolean {
        val tRTC = SystemClock.elapsedRealtime() - mStartRTC
        return when {
            mFinished -> false
            tRTC >= mAnimationDurationMillis -> {
                mFinished = true
                currZoom = mEndZoom
                false
            }
            else -> {
                currZoom =
                    mEndZoom * mInterpolator.getInterpolation(tRTC * 1f / mAnimationDurationMillis)
                true
            }
        }
    }

    companion object {
        private const val DEFAULT_SHORT_ANIMATION_DURATION = 200L
    }
}
