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

package org.kiwix.kiwixmobile.core.page.viewmodel.effects

import androidx.appcompat.app.AppCompatActivity
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kiwix.kiwixmobile.core.dao.PageDao
import org.kiwix.kiwixmobile.core.page.historyItem
import org.kiwix.kiwixmobile.core.page.historyState
import org.kiwix.kiwixmobile.core.reader.ZimReaderSource

internal class DeletePageItemsTest {
  private val pageDao: PageDao = mockk(relaxed = true)
  val activity: AppCompatActivity = mockk()
  private val zimReaderSource: ZimReaderSource = mockk()
  private val item1 = historyItem(zimReaderSource = zimReaderSource)
  private val item2 = historyItem(zimReaderSource = zimReaderSource)
  private val viewModelScope = CoroutineScope(Dispatchers.Main)

  @Test
  fun `delete with selected items only deletes the selected items`() =
    runBlocking {
      retryTest {
        item1.isSelected = true
        DeletePageItems(
          historyState(listOf(item1, item2)),
          pageDao,
          viewModelScope
        ).invokeWith(activity)
        verify { pageDao.deletePages(listOf(item1)) }
      }
    }

  @Test
  fun `delete with no selected items deletes all items`() =
    runBlocking {
      retryTest {
        item1.isSelected = false
        DeletePageItems(
          historyState(listOf(item1, item2)),
          pageDao,
          viewModelScope
        ).invokeWith(activity)
        verify { pageDao.deletePages(listOf(item1, item2)) }
      }
    }

  private fun retryTest(maxRetries: Int = 5, block: () -> Unit) {
    var currentAttempt = 0
    var success = false
    while (currentAttempt < maxRetries && !success) {
      try {
        block()
        success = true
      } catch (e: AssertionError) {
        currentAttempt++
        if (currentAttempt >= maxRetries) {
          throw e
        }
      }
    }
  }
}
