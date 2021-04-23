package com.eilas.newcourseschedule

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.

        BitmapDrawable().apply {
            draw(
                Canvas(
                    Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
                        .apply { eraseColor(Color.parseColor("#F7EED6")) })
                    .apply {
                        drawText("P",
                            25F,
                            25F,
                            Paint().apply {
                                textSize = 10F
                                color = Color.BLACK
                                style = Paint.Style.FILL
                            }
                        )
                    })
        }
    }
}