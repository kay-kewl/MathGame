package com.example.itismydomain

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    protected var buttonRadius: Float = 0f
    protected var radiusInPixels: Float = 0f
    protected lateinit var roundedButtonDrawable: GradientDrawable
    protected lateinit var roundedInvertedButtonDrawable: GradientDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setDecorFitsSystemWindows(false)

        val controller = window.decorView.windowInsetsController
        controller?.hide(WindowInsets.Type.navigationBars())

        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        buttonRadius = sharedPref.getFloat("ButtonRadius", 38f)

        radiusInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            buttonRadius,
            resources.displayMetrics
        )

        roundedButtonDrawable = createRoundedDrawable(radiusInPixels, Color.BLACK)
        roundedInvertedButtonDrawable = createRoundedDrawable(radiusInPixels, Color.WHITE)

    }

    fun createRoundedDrawable(radius: Float, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }
}