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

package org.kiwix.kiwixmobile.zimManager.fileselectView.effects

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.base.SideEffect
import org.kiwix.kiwixmobile.core.extensions.toast
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem
import org.kiwix.kiwixmobile.main.KiwixMainActivity
import org.kiwix.kiwixmobile.nav.destination.library.local.LocalLibraryFragmentDirections.actionNavigationLibraryToNavigationReader

@Suppress("InjectDispatcher")
data class OpenFileWithNavigation(private val bookOnDisk: BooksOnDiskListItem.BookOnDisk) :
  SideEffect<Unit> {
  override fun invokeWith(activity: AppCompatActivity) {
    val zimReaderSource = bookOnDisk.zimReaderSource
    (activity as KiwixMainActivity).lifecycleScope.launch {
      val canOpenInLibkiwix =
        withContext(Dispatchers.IO) {
          zimReaderSource.canOpenInLibkiwix()
        }
      if (!canOpenInLibkiwix) {
        activity.toast(
          activity.getString(R.string.error_file_not_found, zimReaderSource.toDatabase())
        )
      } else {
        activity.navigate(
          actionNavigationLibraryToNavigationReader().apply {
            zimFileUri = zimReaderSource.toDatabase()
          }
        )
      }
    }
  }
}
