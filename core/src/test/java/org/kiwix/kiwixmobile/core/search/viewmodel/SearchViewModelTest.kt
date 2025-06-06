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

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.R.string
import org.kiwix.kiwixmobile.core.base.SideEffect
import org.kiwix.kiwixmobile.core.dao.RecentSearchRoomDao
import org.kiwix.kiwixmobile.core.reader.ZimFileReader
import org.kiwix.kiwixmobile.core.reader.ZimReaderContainer
import org.kiwix.kiwixmobile.core.search.SearchListItem.RecentSearchListItem
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ActivityResultReceived
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ClickedSearchInText
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ConfirmedDelete
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.CreatedWithArguments
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ExitedSearch
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.Filter
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.OnItemClick
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.OnItemLongClick
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.OnOpenInNewTabClick
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ReceivedPromptForSpeechInput
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.ScreenWasStartedFrom
import org.kiwix.kiwixmobile.core.search.viewmodel.Action.StartSpeechInputFailed
import org.kiwix.kiwixmobile.core.search.viewmodel.SearchOrigin.FromWebView
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.DeleteRecentSearch
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.OpenSearchItem
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.PopFragmentBackstack
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.ProcessActivityResult
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.SaveSearchToRecents
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.SearchArgumentProcessing
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.SearchInPreviousScreen
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.ShowDeleteSearchDialog
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.ShowToast
import org.kiwix.kiwixmobile.core.search.viewmodel.effects.StartSpeechInput
import org.kiwix.kiwixmobile.core.utils.dialog.AlertDialogShower
import org.kiwix.libzim.SuggestionSearch

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchViewModelTest {
  private val recentSearchRoomDao: RecentSearchRoomDao = mockk()
  private val zimReaderContainer: ZimReaderContainer = mockk()
  private val searchResultGenerator: SearchResultGenerator = mockk()
  private val zimFileReader: ZimFileReader = mockk()
  private val dialogShower = mockk<AlertDialogShower>(relaxed = true)
  private val testDispatcher = StandardTestDispatcher()
  private val searchMutex: Mutex = mockk()

  lateinit var viewModel: SearchViewModel

  @AfterAll
  fun teardown() {
    Dispatchers.resetMain()
  }

  private lateinit var recentsFromDb: Channel<List<RecentSearchListItem>>

  @BeforeEach
  fun init() {
    Dispatchers.resetMain()
    Dispatchers.setMain(testDispatcher)
    clearAllMocks()
    recentsFromDb = Channel(Channel.UNLIMITED)
    every { zimReaderContainer.zimFileReader } returns zimFileReader
    coEvery {
      searchResultGenerator.generateSearchResults("", zimFileReader)
    } returns null
    every { zimReaderContainer.id } returns "id"
    every { recentSearchRoomDao.recentSearches("id") } returns recentsFromDb.consumeAsFlow()
    viewModel =
      SearchViewModel(
        recentSearchRoomDao,
        zimReaderContainer,
        searchResultGenerator,
        searchMutex
      ).apply {
        setAlertDialogShower(dialogShower)
      }
  }

  @Nested
  inner class StateTests {
    @Test
    fun `initial state is Initialising`() =
      runTest {
        viewModel.state.test(this).assertLastValue(
          SearchState("", SearchResultsWithTerm("", null, searchMutex), emptyList(), FromWebView)
        ).finish()
      }

    @Test
    fun `SearchState combines sources from inputs`() =
      runTest {
        val searchTerm = "searchTerm"
        val searchOrigin = FromWebView
        val suggestionSearch: SuggestionSearch = mockk()
        viewModel.state.test(this, 2)
          .also {
            emissionOf(
              searchTerm = searchTerm,
              suggestionSearch = suggestionSearch,
              databaseResults = listOf(RecentSearchListItem("", "")),
              searchOrigin = searchOrigin
            )
          }
          .assertLastValue(
            SearchState(
              searchTerm,
              SearchResultsWithTerm(searchTerm, suggestionSearch, searchMutex),
              listOf(RecentSearchListItem("", "")),
              searchOrigin
            )
          )
          .finish()
      }
  }

  @Nested
  inner class ActionMapping {
    @Test
    fun `ExitedSearch offers PopFragmentBackstack`() =
      runTest {
        actionResultsInEffects(ExitedSearch, PopFragmentBackstack)
      }

    @Test
    fun `OnItemClick offers Saves and Opens`() =
      runTest {
        val searchListItem = RecentSearchListItem("", "")
        actionResultsInEffects(
          OnItemClick(searchListItem),
          SaveSearchToRecents(
            recentSearchRoomDao,
            searchListItem,
            "id",
            viewModel.viewModelScope
          ),
          OpenSearchItem(searchListItem, false)
        )
      }

    @Test
    fun `OnOpenInNewTabClick offers Saves and Opens in new tab`() =
      runTest {
        val searchListItem = RecentSearchListItem("", "")
        actionResultsInEffects(
          OnOpenInNewTabClick(searchListItem),
          SaveSearchToRecents(
            recentSearchRoomDao,
            searchListItem,
            "id",
            viewModel.viewModelScope
          ),
          OpenSearchItem(searchListItem, true)
        )
      }

    @Test
    fun `OnItemLongClick offers Saves and Opens`() =
      runTest {
        val searchListItem = RecentSearchListItem("", "")
        actionResultsInEffects(
          OnItemLongClick(searchListItem),
          ShowDeleteSearchDialog(searchListItem, viewModel.actions, dialogShower)
        )
      }

    @Test
    fun `ClickedSearchInText offers SearchInPreviousScreen`() =
      runTest {
        actionResultsInEffects(ClickedSearchInText, SearchInPreviousScreen(""))
      }

    @Test
    fun `ConfirmedDelete offers Delete and Toast`() =
      runTest {
        val searchListItem = RecentSearchListItem("", "")
        actionResultsInEffects(
          ConfirmedDelete(searchListItem),
          DeleteRecentSearch(searchListItem, recentSearchRoomDao, viewModel.viewModelScope),
          ShowToast(R.string.delete_specific_search_toast)
        )
      }

    @Test
    fun `CreatedWithArguments offers SearchArgumentProcessing`() =
      runTest {
        val bundle = mockk<Bundle>()
        actionResultsInEffects(
          CreatedWithArguments(bundle),
          SearchArgumentProcessing(bundle, viewModel.actions)
        )
      }

    @Test
    fun `ReceivedPromptForSpeechInput offers StartSpeechInput`() =
      runTest {
        actionResultsInEffects(
          ReceivedPromptForSpeechInput,
          StartSpeechInput(viewModel.actions)
        )
      }

    @Test
    fun `StartSpeechInputFailed offers ShowToast`() =
      runTest {
        actionResultsInEffects(
          StartSpeechInputFailed,
          ShowToast(string.speech_not_supported)
        )
      }

    @Test
    fun `ActivityResultReceived offers ProcessActivityResult`() =
      runTest {
        actionResultsInEffects(
          ActivityResultReceived(0, 1, null),
          ProcessActivityResult(0, 1, null, viewModel.actions)
        )
      }

    private fun TestScope.actionResultsInEffects(
      action: Action,
      vararg effects: SideEffect<*>
    ) {
      if (effects.size > 1) return
      val collectedEffects = mutableListOf<SideEffect<*>>()
      val job =
        launch {
          viewModel.effects.collect {
            collectedEffects.add(it)
          }
        }

      viewModel.actions.trySend(action).isSuccess
      advanceUntilIdle()
      assertThat(collectedEffects).containsExactlyElementsOf(effects.toList())
      job.cancel()
    }
  }

  private fun emissionOf(
    searchTerm: String,
    suggestionSearch: SuggestionSearch,
    databaseResults: List<RecentSearchListItem>,
    searchOrigin: SearchOrigin
  ) {
    coEvery {
      searchResultGenerator.generateSearchResults(searchTerm, zimFileReader)
    } returns suggestionSearch
    viewModel.actions.trySend(Filter(searchTerm)).isSuccess
    recentsFromDb.trySend(databaseResults).isSuccess
    viewModel.actions.trySend(ScreenWasStartedFrom(searchOrigin)).isSuccess
  }
}

fun <T> Flow<T>.test(scope: TestScope, itemCountsToEmitInFlow: Int = 1): TestObserver<T> {
  val observer = TestObserver(scope, this, itemCountsToEmitInFlow)
  scope.launch { observer.startCollecting() }
  return observer
}

fun <T> MutableSharedFlow<T>.test(scope: TestScope, itemCountsToEmitInFlow: Int): TestObserver<T> {
  val observer = TestObserver(scope, this, itemCountsToEmitInFlow)
  scope.launch { observer.startCollecting() }
  return observer
}

class TestObserver<T>(
  private val scope: TestScope,
  private val flow: Flow<T>,
  private val itemCountsToEmitInFlow: Int
) {
  private val values = mutableListOf<T>()
  private val completionChannel = Channel<Unit>()
  private var job: Job? = null

  fun startCollecting() {
    job = scope.launch {
      flow.collect {
        values.add(it)
        completionChannel.trySend(Unit)
      }
    }
  }

  /**
   * Returns the list of values collected from the flow.
   *
   * If [shouldAwaitCompletion] is true, this method will suspend until the flow
   * signals completion, ensuring all values have been collected before returning.
   *
   * @param shouldAwaitCompletion Whether to wait for the flow to finish collecting before returning the results.
   * @return A mutable list of values emitted by the flow.
   */
  suspend fun getValues(shouldAwaitCompletion: Boolean = true): MutableList<T> {
    if (shouldAwaitCompletion) {
      awaitCompletion()
    }
    return values
  }

  private suspend fun awaitCompletion() {
    repeat(itemCountsToEmitInFlow) {
      completionChannel.receive()
    }
  }

  suspend fun assertValues(listValues: MutableList<T>): TestObserver<T> {
    awaitCompletion()
    assertThat(listValues).containsExactlyElementsOf(values)
    return this
  }

  suspend fun containsExactlyInAnyOrder(
    listValues: MutableList<T>,
    vararg values: T
  ): TestObserver<T> {
    assertThat(listValues).containsExactlyInAnyOrder(*values)
    return this
  }

  suspend fun assertLastValue(value: T): TestObserver<T> {
    awaitCompletion()
    assertThat(values.last()).isEqualTo(value)
    return this
  }

  fun finish() {
    job?.cancel()
  }

  suspend fun assertLastValue(value: (T) -> Boolean): TestObserver<T> {
    awaitCompletion()
    assertThat(values.last()).satisfies({ value(it) })
    return this
  }
}
