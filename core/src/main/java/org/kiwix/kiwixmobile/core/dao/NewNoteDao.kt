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

package org.kiwix.kiwixmobile.core.dao

import io.objectbox.Box
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kiwix.kiwixmobile.core.dao.entities.NotesEntity
import org.kiwix.kiwixmobile.core.dao.entities.NotesEntity_
import org.kiwix.kiwixmobile.core.page.adapter.Page
import org.kiwix.kiwixmobile.core.page.notes.adapter.NoteListItem
import org.kiwix.kiwixmobile.core.reader.ZimReaderSource.Companion.fromDatabaseValue
import javax.inject.Inject

class NewNoteDao @Inject constructor(val box: Box<NotesEntity>) : PageDao {
  fun notes(): Flow<List<Page>> =
    box.asFlow(
      box.query {
        order(NotesEntity_.noteTitle)
      }
    ).map {
      it.map { notesEntity ->
        notesEntity.zimFilePath?.let { filePath ->
          // set zimReaderSource for previously saved notes
          fromDatabaseValue(filePath)?.let { zimReaderSource ->
            notesEntity.zimReaderSource = zimReaderSource
          }
        }
        NoteListItem(notesEntity)
      }
    }

  override fun pages(): Flow<List<Page>> = notes()

  override fun deletePages(pagesToDelete: List<Page>) =
    deleteNotes(pagesToDelete as List<NoteListItem>)

  fun saveNote(noteItem: NoteListItem) {
    box.store.callInTx {
      if (doesNotAlreadyExist(noteItem)) {
        box.put(NotesEntity(noteItem))
      }
    }
  }

  fun deleteNotes(noteList: List<NoteListItem>) {
    box.remove(noteList.map(::NotesEntity))
  }

  fun deleteNote(noteUniqueKey: String) {
    box.query {
      equal(
        NotesEntity_.noteTitle,
        noteUniqueKey,
        QueryBuilder.StringOrder.CASE_INSENSITIVE
      )
    }.remove()
  }

  private fun doesNotAlreadyExist(noteItem: NoteListItem) =
    box.query {
      equal(NotesEntity_.noteTitle, noteItem.title, QueryBuilder.StringOrder.CASE_INSENSITIVE)
    }.count() == 0L
}
