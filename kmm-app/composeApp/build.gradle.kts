import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.koin.compiler)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                }
            }
        }
    }

    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.napier)
            implementation(libs.kotlinx.datetime)
            implementation(libs.material.icons.extended)

            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
            implementation(libs.voyager.transitions)

            // Koin for Multiplatform
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.annotations)

            // Ktor
            implementation(libs.ktor.core)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.auth)

            // Kotlinx coroutines

            // DataStore
            implementation(libs.dataStore)
            implementation(libs.dataStore.core)
            implementation(libs.dataStore.preferences)
            implementation(libs.dataStore.preferences.core)
            implementation(libs.atomicfu)

            // Image presentation
            implementation(libs.kamel.image)

            implementation(libs.openai.client)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }
}

android {
    namespace = "org.edward.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.edward.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
        resources.srcDirs("src/androidMain/resources")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

buildConfig {
    val envFile = rootProject.file(".env")
    val envProps = Properties()
    if (envFile.exists()) {
        envFile.inputStream().use { envProps.load(it) }
    }

    packageName("org.edward.app.config")
    className("EnvConfig")

    envProps.forEach { (key, value) ->
        buildConfigField("String", key.toString().uppercase(), "\"$value\"")
    }
}
