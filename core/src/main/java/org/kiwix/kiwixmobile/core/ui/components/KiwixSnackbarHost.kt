/*
 * Kiwix Android
 * Copyright (c) 2025 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.core.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.kiwix.kiwixmobile.core.ui.theme.DenimBlue400
import org.kiwix.kiwixmobile.core.ui.theme.KiwixSnackToastTheme

/**
 * A custom SnackbarHost for displaying snackbars with theme-aware action button colors.
 *
 * This function ensures that the action button color follows the app's theme:
 * - In **light mode**, the action button color is `DenimBlue400`.
 * - In **dark mode**, the action button color is `surface`, similar to the XML-based styling.
 *
 * @param snackbarHostState The state that controls the Snackbar display.
 */
@Composable
fun KiwixSnackbarHost(snackbarHostState: SnackbarHostState) {
  KiwixSnackToastTheme {
    val actionColor = if (isSystemInDarkTheme()) {
      MaterialTheme.colorScheme.surface
    } else {
      DenimBlue400
    }
    SnackbarHost(hostState = snackbarHostState) { snackbarData ->
      Snackbar(
        snackbarData = snackbarData,
        actionColor = actionColor
      )
    }
  }
}
