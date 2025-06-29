/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.kiwix.kiwixmobile.core.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.R
import androidx.compose.ui.unit.Dp

object DimenUtils {
  @JvmStatic fun Context.getToolbarHeight(): Int {
    return resources.getDimensionPixelSize(
      TypedValue().apply {
        theme.resolveAttribute(R.attr.actionBarSize, this, true)
      }.resourceId
    )
  }

  fun Context.dpToPx(dp: Dp): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.value,
      resources.displayMetrics
    ).toInt()
  }

  @JvmStatic fun Activity.getWindowHeight(): Int =
    computedDisplayMetric.heightPixels

  @JvmStatic fun Activity.getWindowWidth(): Int =
    computedDisplayMetric.widthPixels

  private val Activity.computedDisplayMetric
    get() = DisplayMetrics().apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val displayRect = windowManager.currentWindowMetrics.bounds
        widthPixels = displayRect.width()
        heightPixels = displayRect.height()
      } else {
        @Suppress("Deprecation")
        windowManager.defaultDisplay.getMetrics(this)
      }
    }
}
