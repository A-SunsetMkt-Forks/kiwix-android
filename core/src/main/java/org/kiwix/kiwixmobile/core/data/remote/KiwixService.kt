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
@file:Suppress("DEPRECATION")

package org.kiwix.kiwixmobile.core.data.remote

import okhttp3.OkHttpClient
import org.kiwix.kiwixmobile.core.entity.MetaLinkNetworkEntity
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface KiwixService {
  @GET(OPDS_LIBRARY_NETWORK_PATH)
  suspend fun getLibrary(): Response<String>

  @GET
  suspend fun getMetaLinks(
    @Url url: String
  ): MetaLinkNetworkEntity?

  /******** Helper class that sets up new services  */
  object ServiceCreator {
    @Suppress("DEPRECATION")
    fun newHackListService(okHttpClient: OkHttpClient, baseUrl: String): KiwixService {
      val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build()
      return retrofit.create(KiwixService::class.java)
    }
  }

  companion object {
    // To fetch the full OPDS catalog.
    // TODO we will change this to pagination later once we migrate to OPDS properly.
    const val OPDS_LIBRARY_NETWORK_PATH = "v2/entries?count=-1"
  }
}
