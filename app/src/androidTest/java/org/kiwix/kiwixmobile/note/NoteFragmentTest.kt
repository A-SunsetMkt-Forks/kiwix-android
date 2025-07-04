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

import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
import leakcanary.LeakAssertions
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kiwix.kiwixmobile.BaseActivityTest
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.core.utils.LanguageUtils.Companion.handleLocaleChange
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil
import org.kiwix.kiwixmobile.core.utils.TestingUtils.COMPOSE_TEST_RULE_ORDER
import org.kiwix.kiwixmobile.core.utils.TestingUtils.RETRY_RULE_ORDER
import org.kiwix.kiwixmobile.main.KiwixMainActivity
import org.kiwix.kiwixmobile.nav.destination.library.library
import org.kiwix.kiwixmobile.nav.destination.library.local.LocalLibraryFragmentDirections
import org.kiwix.kiwixmobile.testutils.RetryRule
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.closeSystemDialogs
import org.kiwix.kiwixmobile.testutils.TestUtils.isSystemUINotRespondingDialogVisible
import org.kiwix.kiwixmobile.utils.StandardActions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class NoteFragmentTest : BaseActivityTest() {
  @Rule(order = RETRY_RULE_ORDER)
  @JvmField
  val retryRule = RetryRule()

  @get:Rule(order = COMPOSE_TEST_RULE_ORDER)
  val composeTestRule = createComposeRule()

  private lateinit var kiwixMainActivity: KiwixMainActivity

  @Before
  override fun waitForIdle() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).apply {
      if (isSystemUINotRespondingDialogVisible(this)) {
        closeSystemDialogs(context, this)
      }
      waitForIdle()
    }
    PreferenceManager.getDefaultSharedPreferences(context).edit {
      putBoolean(SharedPreferenceUtil.PREF_SHOW_INTRO, false)
      putBoolean(SharedPreferenceUtil.PREF_WIFI_ONLY, false)
      putBoolean(SharedPreferenceUtil.PREF_IS_TEST, true)
      putString(SharedPreferenceUtil.PREF_LANG, "en")
      putLong(
        SharedPreferenceUtil.PREF_LAST_DONATION_POPUP_SHOWN_IN_MILLISECONDS,
        System.currentTimeMillis()
      )
    }
    activityScenario =
      ActivityScenario.launch(KiwixMainActivity::class.java).apply {
        moveToState(Lifecycle.State.RESUMED)
        onActivity {
          handleLocaleChange(
            it,
            "en",
            SharedPreferenceUtil(context)
          )
        }
      }
  }

  init {
    AccessibilityChecks.enable().apply {
      setRunChecksFromRootView(true)
      setSuppressingResultMatcher(
        anyOf(
          allOf(
            matchesCheck(TouchTargetSizeCheck::class.java),
            matchesViews(withContentDescription("More options"))
          ),
          matchesCheck(SpeakableTextPresentCheck::class.java)
        )
      )
    }
  }

  @Test
  fun verifyNoteFragment() {
    activityScenario.onActivity {
      it.navigate(R.id.notesFragment)
    }
    note {
      assertToolbarExist(composeTestRule)
      assertSwitchWidgetExist(composeTestRule)
    }
    LeakAssertions.assertNoLeaks()
  }

  @Test
  fun testUserCanSeeNotesForDeletedFiles() {
    deletePreviouslySavedNotes()
    loadZimFileInReader("testzim.zim")
    StandardActions.closeDrawer() // close the drawer if open before running the test cases.
    note {
      clickOnNoteMenuItem(composeTestRule)
      assertNoteDialogDisplayed(composeTestRule)
      writeDemoNote(composeTestRule)
      saveNote(composeTestRule)
      pressBack()
      openNoteFragment()
      assertToolbarExist(composeTestRule)
      clickOnSavedNote(composeTestRule)
      clickOnOpenNote(composeTestRule)
      assertNoteSaved(composeTestRule)
      // to close the note dialog.
      pressBack()
      // to close the notes fragment.
      pressBack()
    }

    // goto local library fragment to delete the ZIM file
    UiThreadStatement.runOnUiThread {
      kiwixMainActivity.navigate(R.id.libraryFragment)
    }

    library {
      refreshList(composeTestRule)
      waitUntilZimFilesRefreshing(composeTestRule)
      deleteZimIfExists(composeTestRule)
    }

    note {
      openNoteFragment()
      assertToolbarExist(composeTestRule)
      clickOnSavedNote(composeTestRule)
      clickOnOpenNote(composeTestRule)
      assertNoteSaved(composeTestRule)
      pressBack()
    }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
      // temporary disabled on Android 25
      LeakAssertions.assertNoLeaks()
    }
  }

  @Test
  fun testZimFileOpenedAfterOpeningNoteOnNotesScreen() {
    deletePreviouslySavedNotes()
    loadZimFileInReader("testzim.zim")
    note {
      assertHomePageIsLoadedOfTestZimFile()
      clickOnNoteMenuItem(composeTestRule)
      assertNoteDialogDisplayed(composeTestRule)
      writeDemoNote(composeTestRule)
      saveNote(composeTestRule)
      pressBack()
      openNoteFragment()
      assertToolbarExist(composeTestRule)
      clickOnSavedNote(composeTestRule)
      clickOnOpenNote(composeTestRule)
      assertNoteSaved(composeTestRule)
      // to close the note dialog.
      pressBack()
      // to close the notes fragment.
      pressBack()
    }
  }

  @Test
  fun testNoteEntryIsRemovedFromDatabaseWhenDeletedInAddNoteDialog() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
      deletePreviouslySavedNotes()
      loadZimFileInReader("testzim.zim")
      note {
        clickOnNoteMenuItem(composeTestRule)
        assertNoteDialogDisplayed(composeTestRule)
        writeDemoNote(composeTestRule)
        saveNote(composeTestRule)
        pressBack()
        openNoteFragment()
        assertToolbarExist(composeTestRule)
        clickOnSavedNote(composeTestRule)
        clickOnOpenNote(composeTestRule)
        assertNoteSaved(composeTestRule)
        clickOnDeleteIcon(composeTestRule)
        pressBack()
        assertNoNotesTextDisplayed(composeTestRule)
      }
    }
  }

  @Test
  fun testNoteFileIsDeletedWhenNoteIsRemovedFromNotesScreen() {
    deletePreviouslySavedNotes()
    loadZimFileInReader("testzim.zim")
    // Save a note.
    note {
      clickOnNoteMenuItem(composeTestRule)
      assertNoteDialogDisplayed(composeTestRule)
      writeDemoNote(composeTestRule)
      saveNote(composeTestRule)
      pressBack()
    }
    // Delete that note from "Note" screen.
    deletePreviouslySavedNotes()
    // Test the note file is deleted or not.
    note {
      clickOnNoteMenuItem(composeTestRule)
      assertNoteDialogDisplayed(composeTestRule)
      assertNotDoesNotExist(composeTestRule)
      pressBack()
    }
  }

  private fun deletePreviouslySavedNotes() {
    // delete the notes if any saved to properly run the test scenario
    note {
      openNoteFragment()
      assertToolbarExist(composeTestRule)
      clickOnTrashIcon(composeTestRule)
      assertDeleteNoteDialogDisplayed(composeTestRule)
      clickOnDeleteButton(composeTestRule)
      assertNoNotesTextDisplayed(composeTestRule)
      pressBack()
    }
  }

  private fun loadZimFileInReader(zimFileName: String) {
    activityScenario.onActivity {
      kiwixMainActivity = it
      kiwixMainActivity.navigate(R.id.libraryFragment)
    }

    val loadFileStream =
      NoteFragmentTest::class.java.classLoader.getResourceAsStream(zimFileName)
    val zimFile = File(context.getExternalFilesDirs(null)[0], zimFileName)
    if (zimFile.exists()) zimFile.delete()
    zimFile.createNewFile()
    loadFileStream.use { inputStream ->
      val outputStream: OutputStream = FileOutputStream(zimFile)
      outputStream.use { it ->
        val buffer = ByteArray(inputStream.available())
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
          it.write(buffer, 0, length)
        }
      }
    }
    UiThreadStatement.runOnUiThread {
      kiwixMainActivity.navigate(
        LocalLibraryFragmentDirections.actionNavigationLibraryToNavigationReader()
          .apply { zimFileUri = zimFile.toUri().toString() }
      )
    }
  }

  @After
  fun finish() {
    TestUtils.deleteTemporaryFilesOfTestCases(context)
  }
}
