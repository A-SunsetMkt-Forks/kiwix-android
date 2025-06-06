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

import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Status
import org.kiwix.kiwixmobile.core.dao.entities.DownloadRoomEntity
import org.kiwix.kiwixmobile.core.entity.LibkiwixBook
import org.kiwix.kiwixmobile.core.utils.StorageUtils

data class DownloadModel(
  val databaseId: Long,
  val downloadId: Long,
  val file: String?,
  val etaInMilliSeconds: Long,
  val bytesDownloaded: Long,
  val totalSizeOfDownload: Long,
  val state: Status,
  val error: Error,
  val progress: Int,
  val book: LibkiwixBook
) {
  val bytesRemaining: Long by lazy { totalSizeOfDownload - bytesDownloaded }
  val fileNameFromUrl: String by lazy { StorageUtils.getFileNameFromUrl(book.url) }

  constructor(downloadEntity: DownloadRoomEntity) : this(
    downloadEntity.id,
    downloadEntity.downloadId,
    downloadEntity.file,
    downloadEntity.etaInMilliSeconds,
    downloadEntity.bytesDownloaded,
    downloadEntity.totalSizeOfDownload,
    downloadEntity.status,
    downloadEntity.error,
    downloadEntity.progress,
    downloadEntity.toBook()
  )
}
