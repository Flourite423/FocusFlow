// Top-level build file
plugins {
    id("com.android.application") version libs.versions.agp.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("dagger.hilt.android.plugin") version libs.versions.hilt.get() apply false
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get() apply false
}
