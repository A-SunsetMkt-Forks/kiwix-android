plugins {
  `kotlin-dsl`
}
repositories {
  mavenCentral()
  google()
  maven {
    setUrl("https://plugins.gradle.org/m2/")
  }
  maven { setUrl("https://jitpack.io") }
}

dependencies {
  implementation("com.android.tools.build:gradle:8.11.0-alpha03")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
  implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.0-1.0.24")
  implementation("org.jacoco:org.jacoco.core:0.8.12")
  implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.2")
  implementation("com.google.apis:google-api-services-androidpublisher:v3-rev20230406-2.0.0") {
    exclude(group = "com.google.guava", module = "guava")
  }
  implementation("com.google.http-client:google-http-client-jackson2:1.40.0") {
    exclude(group = "com.google.guava", module = "guava")
  }
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
  implementation("com.googlecode.json-simple:json-simple:1.1.1")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  implementation(gradleApi())
  implementation(localGroovy())
}
