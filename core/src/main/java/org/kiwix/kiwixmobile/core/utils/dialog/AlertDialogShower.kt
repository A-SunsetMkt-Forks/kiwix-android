/*
 * Kiwix Android
 * Copyright (c) 2020 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.core.utils.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.R.attr
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.extensions.getAttribute
import org.kiwix.kiwixmobile.core.utils.StyleUtils.fromHtml
import javax.inject.Inject

class AlertDialogShower @Inject constructor(private val activity: Activity) : DialogShower {
  companion object {
    const val EXTERNAL_LINK_LEFT_MARGIN = 10
    const val EXTERNAL_LINK_RIGHT_MARGIN = 10
    const val EXTERNAL_LINK_TOP_MARGIN = 10
    const val EXTERNAL_LINK_BOTTOM_MARGIN = 0
  }

  override fun show(dialog: KiwixDialog, vararg clickListeners: () -> Unit, uri: Uri?) =
    create(dialog, *clickListeners, uri = uri).show()

  override fun create(dialog: KiwixDialog, vararg clickListeners: () -> Unit, uri: Uri?): Dialog {
    return AlertDialog.Builder(activity)
      .apply {
        dialog.title?.let(this::setTitle)
        dialog.icon?.let(this::setIcon)

        dialog.message?.let { setMessage(activity.getString(it, *bodyArguments(dialog))) }
        setPositiveButton(dialog.positiveMessage) { _, _ ->
          clickListeners.getOrNull(0)
            ?.invoke()
        }
        dialog.negativeMessage?.let {
          setNegativeButton(it) { _, _ ->
            clickListeners.getOrNull(1)
              ?.invoke()
          }
        }
        dialog.neutralMessage?.let {
          setNeutralButton(it) { _, _ ->
            clickListeners.getOrNull(2)
              ?.invoke()
          }
        }
        uri?.let {
          val frameLayout = FrameLayout(activity.baseContext)

          val textView = TextView(activity.baseContext).apply {
            layoutParams = getFrameLayoutParams()
            gravity = Gravity.CENTER
            minHeight = resources.getDimensionPixelSize(R.dimen.material_minimum_height_and_width)
            setLinkTextColor(activity.getAttribute(attr.colorPrimary))
            setOnLongClickListener {
              val clipboard =
                ContextCompat.getSystemService(activity.baseContext, ClipboardManager::class.java)
              val clip = ClipData.newPlainText("External Url", "$uri")
              clipboard?.setPrimaryClip(clip)
              Toast.makeText(
                activity.baseContext,
                R.string.external_link_copied_message,
                Toast.LENGTH_SHORT
              ).show()
              true
            }
            @SuppressLint("SetTextI18n")
            text = "</br><a href=$uri> <b>$uri</b>".fromHtml()
          }
          frameLayout.addView(textView)
          setView(frameLayout)
        }
        dialog.getView?.let { setView(it()) }
        setCancelable(dialog.cancelable)
      }
      .create()
  }

  private fun getFrameLayoutParams() = FrameLayout.LayoutParams(
    LayoutParams.MATCH_PARENT,
    LayoutParams.WRAP_CONTENT
  ).apply {
    topMargin = EXTERNAL_LINK_TOP_MARGIN
    bottomMargin = EXTERNAL_LINK_BOTTOM_MARGIN
    leftMargin = EXTERNAL_LINK_LEFT_MARGIN
    rightMargin = EXTERNAL_LINK_RIGHT_MARGIN
  }

  private fun bodyArguments(dialog: KiwixDialog) =
    if (dialog is HasBodyFormatArgs) {
      dialog.args.toTypedArray()
    } else {
      emptyArray()
    }
}
