package com.smartir.remote.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Centralized haptic feedback using View.performHapticFeedback.
 * Requires a View reference (obtained from LocalView in Compose).
 */
object HapticManager {

    /** Light tick for each repeat pulse during hold-to-repeat. */
    fun tick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    /** Confirmation feedback for power toggle. */
    fun confirm(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /** Rejection feedback for errors (e.g., no IR blaster). */
    fun reject(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
