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

package org.kiwix.kiwixmobile.core.search.viewmodel

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.kiwix.kiwixmobile.core.reader.ZimFileReader

internal class ZimSearchResultGeneratorTest {
  private val zimFileReader: ZimFileReader = mockk()

  private val zimSearchResultGenerator: ZimSearchResultGenerator =
    ZimSearchResultGenerator()

  @Test
  internal fun `empty search term returns empty list`() {
    runBlocking {
      assertThat(zimSearchResultGenerator.generateSearchResults("", zimFileReader))
        .isEqualTo(null)
    }
  }

  @Test
  internal fun `suggestion results are distinct`() {
    val searchTerm = " "
    val suggestionSearchWrapper: SuggestionSearchWrapper = mockk()
    every { zimFileReader.searchSuggestions(searchTerm) } returns suggestionSearchWrapper
    runBlocking {
      assertThat(zimSearchResultGenerator.generateSearchResults(searchTerm, zimFileReader))
        .isEqualTo(suggestionSearchWrapper)
      verify {
        zimFileReader.searchSuggestions(searchTerm)
      }
    }
  }
}
