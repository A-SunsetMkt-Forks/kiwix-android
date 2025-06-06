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
package org.kiwix.kiwixmobile.core.dao

import io.objectbox.Box
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kiwix.kiwixmobile.core.dao.entities.HistoryEntity
import org.kiwix.kiwixmobile.core.dao.entities.HistoryEntity_
import org.kiwix.kiwixmobile.core.page.adapter.Page
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem.HistoryItem
import org.kiwix.kiwixmobile.core.reader.ZimReaderSource.Companion.fromDatabaseValue
import javax.inject.Inject

class HistoryDao @Inject constructor(val box: Box<HistoryEntity>) : PageDao {
  fun history(): Flow<List<Page>> =
    box.asFlow(
      box.query {
        orderDesc(HistoryEntity_.timeStamp)
      }
    ).map {
      it.map { historyEntity ->
        historyEntity.zimFilePath?.let { filePath ->
          // set zimReaderSource for previously saved history items
          fromDatabaseValue(filePath)?.let { zimReaderSource ->
            historyEntity.zimReaderSource = zimReaderSource
          }
        }
        HistoryItem(historyEntity)
      }
    }

  override fun pages(): Flow<List<Page>> = history()
  override fun deletePages(pagesToDelete: List<Page>) =
    deleteHistory(pagesToDelete as List<HistoryItem>)

  fun saveHistory(historyItem: HistoryItem) {
    box.store.callInTx {
      box
        .query {
          equal(
            HistoryEntity_.historyUrl,
            historyItem.historyUrl,
            QueryBuilder.StringOrder.CASE_INSENSITIVE
          ).and()
            .equal(
              HistoryEntity_.dateString,
              historyItem.dateString,
              QueryBuilder.StringOrder.CASE_INSENSITIVE
            )
        }
        .remove()
      box.put(HistoryEntity(historyItem))
    }
  }

  fun deleteHistory(historyList: List<HistoryItem>) {
    box.remove(historyList.map(::HistoryEntity))
  }

  fun deleteAllHistory() {
    box.removeAll()
  }
}
