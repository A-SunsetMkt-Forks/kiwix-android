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

package org.kiwix.kiwixmobile.core.extensions

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

@SuppressLint("ShowToast")
fun View.snack(
  stringId: Int,
  anchor: View? = null,
  actionStringId: Int? = null,
  actionClick: (() -> Unit)? = null,
  @ColorInt actionTextColor: Int? = null
) {
  Snackbar.make(
    this,
    stringId,
    Snackbar.LENGTH_LONG
  ).apply {
    actionStringId?.let { setAction(it) { actionClick?.invoke() } }
    actionTextColor?.let(::setActionTextColor)
    anchor?.let {
      anchorView = anchor
      addCallback(
        object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
          override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            transientBottomBar?.anchorView = null
          }
        }
      )
    }
  }.show()
}

@SuppressLint("ShowToast")
fun View.snack(
  message: String,
  anchor: View,
  actionStringId: Int? = null,
  actionClick: (() -> Unit)? = null,
  @ColorInt actionTextColor: Int? = null
) {
  Snackbar.make(
    this,
    message,
    Snackbar.LENGTH_LONG
  ).apply {
    actionStringId?.let { setAction(it) { actionClick?.invoke() } }
    actionTextColor?.let(::setActionTextColor)
    anchorView = anchor
    addCallback(
      object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
          transientBottomBar?.anchorView = null
        }
      }
    )
  }.show()
}

/**
 * Sets the content description to address an accessibility issue reported by the Play Store.
 * Additionally, sets a tooltip for displaying hints to the user when they long-click on the view.
 *
 * @param description The content description and tooltip text to be set.
 */
fun View.setToolTipWithContentDescription(description: String) {
  contentDescription = description
  TooltipCompat.setTooltipText(this, description)
}

fun View.showFullScreenMode(window: Window) {
  WindowCompat.setDecorFitsSystemWindows(window, false)
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    WindowInsetsControllerCompat(window, window.decorView).apply {
      hide(WindowInsetsCompat.Type.systemBars())
      hide(WindowInsetsCompat.Type.displayCutout())
      systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  }
  @Suppress("Deprecation")
  window.apply {
    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
  }
}

fun View.closeFullScreenMode(window: Window) {
  WindowCompat.setDecorFitsSystemWindows(window, false)
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    WindowInsetsControllerCompat(window, window.decorView).apply {
      show(WindowInsetsCompat.Type.systemBars())
      show(WindowInsetsCompat.Type.displayCutout())
    }
  }
  @Suppress("DEPRECATION")
  window.apply {
    addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
  }
}

/**
 * Applies edge-to-edge insets to the current view by adjusting its margins
 * to account for system bars and display cutouts (e.g., status bar, navigation bar, and notches).
 *
 * This method ensures that the view avoids overlapping with system UI components by dynamically
 * setting margins based on the insets provided by the system.
 *
 * Usage: Call this method on any view to apply edge-to-edge handling.
 */
fun View?.applyEdgeToEdgeInsets() {
  this?.let {
    ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
      val systemBarsInsets =
        windowInsets.getInsets(
          WindowInsetsCompat.Type.displayCutout() or
            WindowInsetsCompat.Type.systemBars()
        )
      view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        topMargin = systemBarsInsets.top
        leftMargin = systemBarsInsets.left
        bottomMargin = systemBarsInsets.bottom
        rightMargin = systemBarsInsets.right
      }
      WindowInsetsCompat.CONSUMED
    }
  }
}
