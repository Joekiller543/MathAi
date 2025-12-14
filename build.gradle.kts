File: build.gradle.kts
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // Help IDEs that struggle with version catalog accessors in the plugins block
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```