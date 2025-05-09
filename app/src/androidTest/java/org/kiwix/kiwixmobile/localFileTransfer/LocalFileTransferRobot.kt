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

package org.kiwix.kiwixmobile.localFileTransfer

import android.util.Log
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import applyWithViewHierarchyPrinting
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import org.kiwix.kiwixmobile.BaseRobot
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.core.R.string
import org.kiwix.kiwixmobile.core.ui.components.SHOWCASE_VIEW_MESSAGE_TESTING_TAG
import org.kiwix.kiwixmobile.core.ui.components.SHOWCASE_VIEW_NEXT_BUTTON_TESTING_TAG
import org.kiwix.kiwixmobile.core.ui.components.TOOLBAR_TITLE_TESTING_TAG
import org.kiwix.kiwixmobile.core.page.SEARCH_ICON_TESTING_TAG
import org.kiwix.kiwixmobile.core.utils.dialog.ALERT_DIALOG_MESSAGE_TEXT_TESTING_TAG
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.testFlakyView

/**
 * Authored by Ayush Shrivastava on 29/10/20
 */

fun localFileTransfer(func: LocalFileTransferRobot.() -> Unit) =
  LocalFileTransferRobot().applyWithViewHierarchyPrinting(func)

class LocalFileTransferRobot : BaseRobot() {
  fun assertReceiveFileTitleVisible(composeContentTestRule: ComposeContentTestRule) {
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(TOOLBAR_TITLE_TESTING_TAG)
        .assertTextEquals(context.getString(R.string.receive_files_title))
    }
  }

  fun assertSearchDeviceMenuItemVisible(composeContentTestRule: ComposeContentTestRule) {
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SEARCH_ICON_TESTING_TAG).assertExists()
    }
  }

  fun clickOnSearchDeviceMenuItem(composeContentTestRule: ComposeContentTestRule) {
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SEARCH_ICON_TESTING_TAG).performClick()
    }
  }

  fun assertLocalFileTransferScreenVisible(composeContentTestRule: ComposeContentTestRule) {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS_FOR_DOWNLOAD_TEST.toLong())
    closeEnableWifiP2PDialogIfVisible(composeContentTestRule)
    assertReceiveFileTitleVisible(composeContentTestRule)
  }

  private fun closeEnableWifiP2PDialogIfVisible(composeContentTestRule: ComposeContentTestRule) {
    try {
      testFlakyView({
        composeContentTestRule
          .onNodeWithTag(ALERT_DIALOG_MESSAGE_TEXT_TESTING_TAG)
          .assertTextEquals(context.getString(string.request_enable_wifi))
        pressBack()
      })
    } catch (_: Throwable) {
      Log.i(
        "LOCAL_FILE_TRANSFER_TEST",
        "Couldn't found WIFI P2P dialog, probably this is not exist"
      )
    }
  }

  fun assertLocalLibraryVisible(composeContentTestRule: ComposeContentTestRule) {
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(TOOLBAR_TITLE_TESTING_TAG)
        .assertTextEquals(context.getString(string.library))
    }
  }

  fun assertClickNearbyDeviceMessageVisible(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_MESSAGE_TESTING_TAG)
        .assertTextEquals(context.getString(string.click_nearby_devices_message))
    }
  }

  fun clickOnNextButton(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_NEXT_BUTTON_TESTING_TAG)
        .performClick()
    }
  }

  fun assertDeviceNameMessageVisible(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_MESSAGE_TESTING_TAG)
        .assertTextEquals(context.getString(string.your_device_name_message))
    }
  }

  fun assertNearbyDeviceListMessageVisible(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_MESSAGE_TESTING_TAG)
        .assertTextEquals(context.getString(string.nearby_devices_list_message))
    }
  }

  fun assertTransferZimFilesListMessageVisible(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_MESSAGE_TESTING_TAG)
        .assertTextEquals(context.getString(string.transfer_zim_files_list_message))
    }
  }

  fun assertClickNearbyDeviceMessageNotVisible(composeContentTestRule: ComposeContentTestRule) {
    pauseForBetterTestPerformance()
    composeContentTestRule.apply {
      waitForIdle()
      onNodeWithTag(SHOWCASE_VIEW_MESSAGE_TESTING_TAG)
        .assertDoesNotExist()
    }
  }

  private fun pauseForBetterTestPerformance() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS.toLong())
  }
}
