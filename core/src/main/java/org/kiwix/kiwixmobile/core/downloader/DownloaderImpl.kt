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

package org.kiwix.kiwixmobile.core.downloader

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.kiwix.kiwixmobile.core.dao.DownloadRoomDao
import org.kiwix.kiwixmobile.core.data.remote.KiwixService
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity
import org.kiwix.kiwixmobile.core.entity.LibraryNetworkEntity.Book
import javax.inject.Inject

class DownloaderImpl @Inject constructor(
  private val downloadRequester: DownloadRequester,
  private val downloadRoomDao: DownloadRoomDao,
  private val kiwixService: KiwixService
) : Downloader {
  @Suppress("CheckResult", "IgnoredReturnValue")
  override fun download(book: LibraryNetworkEntity.Book) {
    urlProvider(book)
      .take(1)
      .subscribeOn(Schedulers.io())
      .subscribe(
        {
          downloadRoomDao.addIfDoesNotExist(it, book, downloadRequester)
        },
        Throwable::printStackTrace
      )
  }

  @Suppress("UnsafeCallOnNullableType")
  private fun urlProvider(book: Book): Observable<String> =
    if (book.url?.endsWith("meta4") == true) {
      kiwixService.getMetaLinks(book.url!!)
        .map { it.relevantUrl.value }
    } else {
      Observable.just(book.url)
    }

  override fun cancelDownload(downloadId: Long) {
    downloadRequester.cancel(downloadId)
  }

  override fun retryDownload(downloadId: Long) {
    downloadRequester.retryDownload(downloadId)
  }

  override fun pauseResumeDownload(downloadId: Long, isPause: Boolean) {
    downloadRequester.pauseResumeDownload(downloadId, isPause)
  }
}
