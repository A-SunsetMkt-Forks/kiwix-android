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
package org.kiwix.kiwixmobile.core.downloader.model

import android.content.Context
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.Status.ADDED
import com.tonyodev.fetch2.Status.CANCELLED
import com.tonyodev.fetch2.Status.COMPLETED
import com.tonyodev.fetch2.Status.DELETED
import com.tonyodev.fetch2.Status.DOWNLOADING
import com.tonyodev.fetch2.Status.FAILED
import com.tonyodev.fetch2.Status.NONE
import com.tonyodev.fetch2.Status.PAUSED
import com.tonyodev.fetch2.Status.QUEUED
import com.tonyodev.fetch2.Status.REMOVED
import org.kiwix.kiwixmobile.core.R

data class DownloadItem(
  val downloadId: Long,
  val favIconUrl: String,
  val title: String,
  val description: String?,
  val bytesDownloaded: Long,
  val totalSizeBytes: Long,
  val progress: Int,
  val eta: Seconds,
  val downloadState: DownloadState
) {
  val readableEta: CharSequence = eta.takeIf { it.seconds > 0L }?.toHumanReadableTime().orEmpty()

  constructor(downloadModel: DownloadModel) : this(
    downloadModel.downloadId,
    downloadModel.book.favicon,
    downloadModel.book.title,
    downloadModel.book.description,
    downloadModel.bytesDownloaded,
    downloadModel.totalSizeOfDownload,
    downloadModel.progress,
    Seconds(downloadModel.etaInMilliSeconds / 1000L),
    DownloadState.from(
      downloadModel.state,
      downloadModel.error,
      downloadModel.book.url
    )
  )
}

sealed class DownloadState(
  private val stringId: Int,
  open val zimUrl: String? = null
) {
  companion object {
    fun from(state: Status, error: Error, zimUrl: String?): DownloadState =
      when (state) {
        NONE,
        ADDED,
        QUEUED -> Pending

        DOWNLOADING -> Running
        PAUSED -> Paused
        COMPLETED -> Successful
        CANCELLED,
        FAILED,
        REMOVED,
        DELETED -> Failed(error, zimUrl)
      }
  }

  object Pending : DownloadState(R.string.pending_state)
  object Running : DownloadState(R.string.running_state)
  object Successful : DownloadState(R.string.complete)
  object Paused : DownloadState(R.string.paused_state)
  data class Failed(val reason: Error, override val zimUrl: String?) :
    DownloadState(R.string.failed_state, zimUrl)

  override fun toString(): String = javaClass.simpleName

  fun toReadableState(context: Context): CharSequence =
    when (this) {
      is Failed -> context.getString(stringId, reason.name)
      Pending,
      Running,
      Paused,
      Successful -> context.getString(stringId)
    }
}
