/*
 * Kiwix Android
 * Copyright (c) 2022 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.note

import android.content.Context
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.Locator
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import org.kiwix.kiwixmobile.BaseRobot
import org.kiwix.kiwixmobile.Findable.StringId.TextId
import org.kiwix.kiwixmobile.Findable.Text
import org.kiwix.kiwixmobile.Findable.ViewId
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.main.ADD_NOTE_TEXT_FILED_TESTING_TAG
import org.kiwix.kiwixmobile.core.main.DELETE_MENU_BUTTON_TESTING_TAG
import org.kiwix.kiwixmobile.core.main.SAVE_MENU_BUTTON_TESTING_TAG
import org.kiwix.kiwixmobile.core.ui.components.TOOLBAR_TITLE_TESTING_TAG
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.testFlakyView
import org.kiwix.kiwixmobile.utils.StandardActions.openDrawer

fun note(func: NoteRobot.() -> Unit) = NoteRobot().apply(func)

class NoteRobot : BaseRobot() {
  private val noteText = "Test Note"

  fun assertToolbarExist() {
    isVisible(ViewId(R.id.toolbar))
  }

  fun assertNoteRecyclerViewExist() {
    isVisible(ViewId(R.id.recycler_view))
  }

  fun assertSwitchWidgetExist() {
    isVisible(ViewId(R.id.page_switch))
  }

  fun clickOnNoteMenuItem(context: Context) {
    // wait a bit to properly load the readerFragment and setting up the
    // overFlowOptionMenu so that we can easily click on it.
    pauseForBetterTestPerformance()
    openActionBarOverflowOrOptionsMenu(context)
    clickOn(TextId(R.string.take_notes))
  }

  fun assertNoteDialogDisplayed(composeTestRule: ComposeContentTestRule) {
    testFlakyView({
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(TOOLBAR_TITLE_TESTING_TAG)
        .assertTextEquals(context.getString(R.string.note))
    })
  }

  fun writeDemoNote(composeTestRule: ComposeContentTestRule) {
    testFlakyView({
      composeTestRule.waitForIdle()
      // Click on the TextField to focus it
      composeTestRule.onNodeWithTag(ADD_NOTE_TEXT_FILED_TESTING_TAG)
        .assertExists("TextField not found in dialog")
        .performClick()
        .performTextReplacement(noteText)

      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag(ADD_NOTE_TEXT_FILED_TESTING_TAG)
        .assertTextContains(noteText, substring = true)

      // Close the keyboard after typing
      closeSoftKeyboard()
    })
  }

  fun saveNote(composeTestRule: ComposeContentTestRule) {
    testFlakyView({
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(SAVE_MENU_BUTTON_TESTING_TAG)
        .performClick()
    })
  }

  fun openNoteFragment() {
    openDrawer()
    testFlakyView({ onView(withText(R.string.pref_notes)).perform(click()) })
  }

  fun clickOnSavedNote() {
    testFlakyView({
      onView(withId(R.id.recycler_view)).perform(
        actionOnItemAtPosition<RecyclerView.ViewHolder>(
          0,
          click()
        )
      )
    })
  }

  fun clickOnOpenNote() {
    testFlakyView({ clickOn(Text("OPEN NOTE")) })
  }

  fun assertNoteSaved(composeTestRule: ComposeContentTestRule) {
    // This is flaky since it is shown in a dialog and sometimes
    // UIDevice does not found the view immediately due to rendering process.
    testFlakyView({
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(ADD_NOTE_TEXT_FILED_TESTING_TAG)
        .assertTextEquals(noteText)
    })
  }

  fun assertNotDoesNotExist(composeTestRule: ComposeContentTestRule) {
    testFlakyView({
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(ADD_NOTE_TEXT_FILED_TESTING_TAG)
        .assertTextContains("", ignoreCase = true)
    })
  }

  fun clickOnDeleteIcon(composeTestRule: ComposeContentTestRule) {
    testFlakyView({
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(DELETE_MENU_BUTTON_TESTING_TAG)
        .performClick()
    })
  }

  fun clickOnTrashIcon() {
    testFlakyView({ onView(withContentDescription(R.string.pref_clear_notes)).perform(click()) })
  }

  fun assertDeleteNoteDialogDisplayed() {
    testFlakyView({ isVisible(TextId(R.string.delete_notes_confirmation_msg)) })
  }

  fun clickOnDeleteButton() {
    pauseForBetterTestPerformance()
    testFlakyView({ onView(ViewMatchers.withText("DELETE")).perform(click()) })
  }

  fun assertNoNotesTextDisplayed() {
    testFlakyView({ isVisible(TextId(R.string.no_notes)) })
  }

  fun assertHomePageIsLoadedOfTestZimFile() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onWebView()
        .withElement(
          findElement(
            Locator.XPATH,
            "//*[contains(text(), 'Android_(operating_system)')]"
          )
        )
    })
  }

  private fun pauseForBetterTestPerformance() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS_FOR_SEARCH_TEST.toLong())
  }
}
