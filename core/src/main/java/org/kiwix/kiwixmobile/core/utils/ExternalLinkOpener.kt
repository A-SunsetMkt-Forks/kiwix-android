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

package org.kiwix.kiwixmobile.core.utils

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.extensions.toast
import org.kiwix.kiwixmobile.core.utils.dialog.AlertDialogShower
import org.kiwix.kiwixmobile.core.utils.dialog.KiwixDialog
import javax.inject.Inject

class ExternalLinkOpener @Inject constructor(
  private val activity: Activity,
  private val sharedPreferenceUtil: SharedPreferenceUtil
) {
  private lateinit var alertDialogShower: AlertDialogShower

  fun setAlertDialogShower(alertDialogShower: AlertDialogShower) {
    this.alertDialogShower = alertDialogShower
  }

  fun openExternalUrl(
    intent: Intent,
    showExternalLinkPopup: Boolean = sharedPreferenceUtil.prefExternalLinkPopup
  ) {
    if (intent.resolveActivity(activity.packageManager) != null) {
      if (showExternalLinkPopup) {
        requestOpenLink(intent)
      } else {
        openLink(intent)
      }
    } else {
      activity.toast(R.string.no_reader_application_installed)
    }
  }

  private fun openLink(intent: Intent) {
    activity.startActivity(intent)
  }

  private fun requestOpenLink(intent: Intent) {
    alertDialogShower.show(
      KiwixDialog.ExternalLinkPopup,
      { openLink(intent) },
      { },
      {
        sharedPreferenceUtil.putPrefExternalLinkPopup(false)
        openLink(intent)
      },
      uri = intent.data
    )
  }

  fun showTTSLanguageDownloadDialog() {
    alertDialogShower.show(
      KiwixDialog.DownloadTTSLanguage,
      {
        activity.startActivity(
          Intent().apply {
            action = ACTION_INSTALL_TTS_DATA
          }
        )
      }
    )
  }
}
