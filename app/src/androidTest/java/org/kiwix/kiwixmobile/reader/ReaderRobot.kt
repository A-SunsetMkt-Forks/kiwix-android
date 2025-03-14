/*
 * Kiwix Android
 * Copyright (c) 2023 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.reader

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import applyWithViewHierarchyPrinting
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import org.kiwix.kiwixmobile.BaseRobot
import org.kiwix.kiwixmobile.Findable.Text
import org.kiwix.kiwixmobile.Findable.ViewId
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.testFlakyView

fun reader(func: ReaderRobot.() -> Unit) = ReaderRobot().applyWithViewHierarchyPrinting(func)

class ReaderRobot : BaseRobot() {
  private var retryCountForClickOnUndoButton = 5

  fun checkZimFileLoadedSuccessful(readerFragment: Int) {
    pauseForBetterTestPerformance()
    isVisible(ViewId(readerFragment))
  }

  fun clickOnTabIcon() {
    pauseForBetterTestPerformance()
    testFlakyView({ onView(withId(R.id.ic_tab_switcher_text)).perform(click()) })
  }

  fun clickOnClosedAllTabsButton() {
    pauseForBetterTestPerformance()
    clickOn(ViewId(R.id.tab_switcher_close_all_tabs))
  }

  fun clickOnUndoButton() {
    try {
      onView(withText("UNDO")).perform(click())
    } catch (runtimeException: RuntimeException) {
      if (retryCountForClickOnUndoButton > 0) {
        retryCountForClickOnUndoButton--
        clickOnUndoButton()
      }
    }
  }

  fun assertTabRestored() {
    pauseForBetterTestPerformance()
    isVisible(Text("Test Zim"))
  }

  private fun pauseForBetterTestPerformance() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS_FOR_SEARCH_TEST.toLong())
  }

  fun clickOnArticle(articleTitle: String) {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS.toLong())
    testFlakyView({
      onWebView()
        .withElement(
          findElement(
            Locator.XPATH,
            "//*[contains(text(), '$articleTitle')]"
          )
        )
        .perform(webClick())
    })
  }

  fun assertArticleLoaded(articlePageContent: String) {
    testFlakyView({
      onWebView()
        .withElement(
          findElement(
            Locator.XPATH,
            "//*[contains(text(), '$articlePageContent')]"
          )
        )
    })
  }
}
