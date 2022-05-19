buildscript {
  repositories {
    google()
    mavenCentral()
    maven {
      url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
      credentials {
        username = "mapbox"
        password = System.getenv("SDK_REGISTRY_TOKEN") ?: project.property("SDK_REGISTRY_TOKEN") as String
      }
      authentication {
        create<BasicAuthentication>("basic")
      }
    }
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath(Plugins.android)
    classpath(Plugins.kotlin)
    classpath(Plugins.jacoco)
    classpath(Plugins.license)
    classpath(Plugins.mapboxAccessToken)
    classpath(Plugins.mapboxSdkRegistry)
    classpath(Plugins.mapboxSdkVersionsPlugin)
    classpath(Plugins.pitestPlugin)
    classpath(Plugins.detekt)
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven {
      url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
      credentials {
        username = "mapbox"
        password = System.getenv("SDK_REGISTRY_TOKEN") ?: project.property("SDK_REGISTRY_TOKEN") as String
      }
      authentication {
        create<BasicAuthentication>("basic")
      }
    }
    maven {
      url = uri("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }
  }
  apply(from = "$rootDir/gradle/detekt.gradle")

  dependencies {
    "detektPlugins"(Dependencies.detektFormatting)
    "detektPlugins"(Dependencies.detektCLI)
  }
}

plugins {
  id(Plugins.dokkaId) version Versions.pluginDokka
  id(Plugins.binaryCompatibilityValidatorId) version Versions.pluginBinaryCompatibilityValidator
  // Used to print dependency tree of the task, useful to debug gradle tasks
  // Ticket to track adding this feature to gradle officially: https://github.com/gradle/gradle/issues/980
  id(Plugins.taskTreeId) version Versions.pluginTaskTree

}
repositories {
  maven(url = "https://dl.bintray.com/kotlin/dokka")
}
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
  outputDirectory.set(buildDir.resolve(this.name))
}
tasks.withType<Test> {
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

apiValidation {
  /**
   * Packages that are excluded from public API dumps even if they
   * contain public API.
   */
//  ignoredPackages.add("kotlinx.coroutines.internal")

  /**
   * Sub-projects that are excluded from API validation
   */
  ignoredProjects.addAll(listOf("extension-style-app", "android-auto-app", "app"))

  /**
   * Classes (fully qualified) that are excluded from public API dumps even if they
   * contain public API.
   */
  ignoredClasses.addAll(
    listOf(
      "com.mapbox.maps.extension.androidauto.BuildConfig",
      "com.mapbox.maps.extension.style.BuildConfig",
      "com.mapbox.maps.extension.localization.BuildConfig",
      "com.mapbox.maps.module.telemetry.BuildConfig",
      "com.mapbox.maps.plugin.camera.animation.BuildConfig",
      "com.mapbox.maps.plugin.annotation.BuildConfig",
      "com.mapbox.maps.plugin.attribution.BuildConfig",
      "com.mapbox.maps.plugin.compass.BuildConfig",
      "com.mapbox.maps.plugin.gestures.BuildConfig",
      "com.mapbox.maps.plugin.lifecycle.BuildConfig",
      "com.mapbox.maps.plugin.locationcomponent.BuildConfig",
      "com.mapbox.maps.plugin.logo.BuildConfig",
      "com.mapbox.maps.plugin.overlay.BuildConfig",
      "com.mapbox.maps.plugin.scalebar.BuildConfig",
      "com.mapbox.maps.plugin.viewport.BuildConfig",
      "com.mapbox.maps.BuildConfig",
      "com.mapbox.maps.base.BuildConfig"
    )
  )

  /**
   * Set of annotations that exclude API from being public.
   * Typically, it is all kinds of `@InternalApi` annotations that mark
   * effectively private API that cannot be actually private for technical reasons.
   */
//  nonPublicMarkers.add("com.mapbox.maps.MapboxExperimental")

  /**
   * Flag to programmatically disable compatibility validator
   */
  validationDisabled = false
}

val detektAll by tasks.registering(io.gitlab.arturbosch.detekt.Detekt::class) {
  configureDetekt(this)
}

val detektFixAll by tasks.registering(io.gitlab.arturbosch.detekt.Detekt::class) {
  configureDetekt(this)
  autoCorrect = true
}

fun configureDetekt(detekt: io.gitlab.arturbosch.detekt.Detekt) {
  detekt.apply {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    buildUponDefaultConfig = true
    setSource(files(rootDir))
    config.from(files("$rootDir/detekt.yml"))
    allRules = false
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/res/**")
    exclude("**/build/**")
    val reportsDir = "${rootProject.buildDir}/reports"
    reports {
      xml {
        enabled = true
        destination = file("$reportsDir/detekt.xml")
      }
      html {
        enabled = true
        destination = file("$reportsDir/detekt.html")
      }
      custom {
        reportId = "CustomJsonReport"
        destination = file("$reportsDir/detekt.json")
      }
    }
  }
}
