
package com.example.bookstore.ui

import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookstore.R

object SystemNavigationBarStyler {

    fun apply(activity: AppCompatActivity) {

        val window = activity.window

        val brownColor = ContextCompat.getColor(
            activity,
            R.color.primary
        )

        // Disable navigation bar contrast
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }

        // Set navigation bar icons to light appearance
        if (Build.VERSION.SDK_INT >= 26) {
            WindowCompat.getInsetsController(
                window,
                window.decorView
            ).isAppearanceLightNavigationBars = false
        }

        if (Build.VERSION.SDK_INT >= 35) {

            // Enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(
                window,
                false
            )

            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT

            // Use DecorView instead of android.R.id.content
            val decorView = window.decorView as FrameLayout

            val bottomBackground = View(activity).apply {
                setBackgroundColor(brownColor)
                isClickable = false
                isFocusable = false
            }

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0,
                Gravity.BOTTOM
            )

            decorView.addView(bottomBackground, params)

            ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->

                val navigationHeight = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                ).bottom

                params.height = navigationHeight
                bottomBackground.layoutParams = params

                insets
            }

            ViewCompat.requestApplyInsets(decorView)

        } else {

            @Suppress("DEPRECATION")
            window.navigationBarColor = brownColor
        }
    }
}
