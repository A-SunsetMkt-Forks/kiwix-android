/*
 * Kiwix Android
 * Copyright (c) 2024 Kiwix <android.kiwix.org>
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
package org.kiwix.kiwixmobile.webserver

import org.kiwix.kiwixmobile.core.base.BaseContract
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem

class ZimHostContract {
  interface View : BaseContract.View<Presenter> {
    suspend fun addBooks(books: List<BooksOnDiskListItem>)
  }

  interface Presenter : BaseContract.Presenter<View> {
    suspend fun loadBooks(previouslyHostedBooks: Set<String>)
  }
}
