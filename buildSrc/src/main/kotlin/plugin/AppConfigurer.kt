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

package plugin

import Libs
import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.project
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppConfigurer {
  fun configure(target: Project) {
    target.configureExtension<AppExtension> {
      signingConfigs {
        create("releaseSigningConfig") {
          storeFile = File(target.rootDir, "kiwix-android.keystore")
          storePassword = System.getenv("KEY_STORE_PASSWORD") ?: "000000"
          keyAlias = System.getenv("KEY_ALIAS") ?: "keystore"
          keyPassword = System.getenv("KEY_PASSWORD") ?: "000000"
        }
      }
      buildTypes {
        getByName("release") {
          isMinifyEnabled = true
          isShrinkResources = true
          signingConfig = signingConfigs.getByName("releaseSigningConfig")
          proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            File("${target.rootDir}/app", "proguard-rules.pro")
          )
        }
        getByName("debug") {
          if (target.hasProperty("testingMinimizedBuild")) {
            isMinifyEnabled = target.hasProperty("testingMinimizedBuild")
            isShrinkResources = target.hasProperty("testingMinimizedBuild")
            proguardFiles(
              getDefaultProguardFile("proguard-android-optimize.txt"),
              File("${target.rootDir}/app", "proguard-rules.pro")
            )
            testProguardFile(File("${target.rootDir}/app", "test-rules.pro"))
          }
        }
      }

      val abiCodes = mapOf("arm64-v8a" to 6, "x86" to 3, "x86_64" to 4, "armeabi-v7a" to 5)
      splits {
        abi {
          // Enable ABI splits only when needed (e.g., when building APKs).
          // This prevents unnecessary splits when generating an App Bundle (AAB),
          // as AABs already handle ABI splits automatically.
          //
          // The environment variable `APK_BUILD` controls this behavior:
          // - If set to `"true"`, ABI splits are **enabled** (for APK builds).
          // - If `"false"` or unset, ABI splits are **disabled** (for App Bundles).
          //
          // This approach ensures that:
          // - **App Bundles (AABs)** remain unaffected (since Google Play handles ABI splits).
          // - **APK builds** get ABI splits when needed for direct distribution (e.g., custom deployments).
          //
          // See: https://github.com/kiwix/kiwix-android/issues/4273
          isEnable = System.getenv("APK_BUILD")?.toBoolean() ?: false
          reset()
          include(*abiCodes.keys.toTypedArray())
          isUniversalApk = true
        }
      }

      /*
       * Leads the version code with a one digit number corresponding
       * to the architecture (arm64-v8a, armeabi-v7a, x86_64, x86,
       * ...). If no number/architecture is found, then add "7". This is
       * should happen only for bundle (it is necessary for the Play
       * Store upgrade process that the version code is higher than
       * for APKs).
      */
      applicationVariants.all {
        @Suppress("DEPRECATION")
        outputs.filterIsInstance<ApkVariantOutput>().forEach { output: ApkVariantOutput ->
          val abiVersionCode = abiCodes[output.getFilter(VariantOutput.FilterType.ABI)] ?: 7
          output.versionCodeOverride = abiVersionCode * 1_000_000 + output.versionCode
          if (output.outputFileName.contains("universal-nightly")) {
            // this is for issue https://github.com/kiwix/kiwix-android/issues/3103
            output.outputFileName = setNameForNightlyUniversalApk()
          }
        }
      }

      aaptOptions {
        cruncherEnabled = true
      }
    }
    configureDependencies(target)
  }

  private fun setNameForNightlyUniversalApk(): String =
    "org.kiwix.kiwixmobile.standalone-universal-${getCurrentDate()}.apk"

  private fun getCurrentDate() =
    Date().let(SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)::format)

  private fun configureDependencies(target: Project) {
    target.dependencies {
      add("implementation", project(":core"))
      androidTestImplementation(Libs.espresso_core)
      androidTestImplementation(Libs.espresso_web)
      androidTestImplementation(Libs.espresso_intents)
      androidTestImplementation(Libs.espresso_contrib)
      androidTestImplementation(Libs.espresso_accessibility)
      androidTestImplementation(Libs.annotation)
      androidTestImplementation(Libs.junit)
      androidTestImplementation(Libs.junit_jupiter)
      androidTestImplementation(Libs.androidx_test_runner)
      androidTestImplementation(Libs.androidx_test_rules)
      androidTestImplementation(Libs.androidx_test_core)
      androidTestImplementation(Libs.mockwebserver)
      androidTestImplementation(Libs.barista) {
        exclude(group = "com.android.support.test.uiautomator")
      }
      androidTestImplementation(Libs.simple_xml) {
        exclude(module = "stax")
        exclude(module = "stax-api")
        exclude(module = "xpp3")
      }
      androidTestUtil(Libs.orchestrator)
      androidTestCompileOnly(Libs.javax_annotation_api)
      kaptAndroidTest(Libs.dagger_compiler)
      androidTestImplementation(Libs.mockk_android)
      androidTestImplementation(Libs.uiautomator)
      androidTestImplementation(Libs.assertj_core)
    }
  }
}
