package com.example.soundhabit.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

object ViewUtil {
    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
            .roundToInt()
    }
}