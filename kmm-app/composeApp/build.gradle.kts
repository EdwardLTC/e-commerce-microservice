import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.util.Properties

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                }
            }
        }
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @OptIn(ExperimentalKotlinGradlePluginApi::class) instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                debugImplementation(libs.androidx.testManifest)
                implementation(libs.androidx.junit4)
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

            // Koin for Multiplatform
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Ktor
            implementation(libs.ktor.core)
            implementation(libs.ktor.negotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.auth)

            // Kotlinx coroutines

            // DataStore
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)
            implementation(libs.dataStore.preferences.core)
            implementation(libs.atomicfu)

            // Image presentation
            implementation(libs.kamel.image)

            implementation(libs.openai.client)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
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
        minSdk = 24
        targetSdk = 36

        applicationId = "org.edward.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }
    //https://developer.android.com/studio/test/gradle-managed-devices
    @Suppress("UnstableApiUsage") testOptions {
        managedDevices.allDevices {
            maybeCreate<ManagedVirtualDevice>("pixel5").apply {
                device = "Pixel 5"
                apiLevel = 36
                systemImageSource = "aosp"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        //enables a Compose tooling support in the AndroidStudio
        compose = true
    }
    buildToolsVersion = "36.0.0"
}

buildConfig {
    val envFile = rootProject.file(".env")
    val envProps = Properties()
    if (envFile.exists()) {
        envFile.inputStream().use { envProps.load(it) }
    }

    val generateEnvConfig by tasks.registering {
        val outputDir = project.layout.buildDirectory.dir("generated/env")
        inputs.file(envFile)
        outputs.dir(outputDir)

        doLast {
            val file = outputDir.get().file("EnvConfig.kt").asFile
            file.parentFile.mkdirs()
            file.writeText(buildString {
                appendLine("package org.edward.app.config")
                appendLine("")
                appendLine("object EnvConfig {")
                envProps.forEach { (key, value) ->
                    appendLine("    const val ${key.toString().uppercase()} = \"${value}\"")
                }
                appendLine("}")
            })
        }
    }

    kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(
        project.layout.buildDirectory.dir("generated/env")
    )

    tasks.named("compileKotlinMetadata").configure {
        dependsOn(generateEnvConfig)
    }
}
