import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val version = Properties().apply { rootProject.file("version.properties").inputStream().use { load(it) } }
android {
    namespace = "fi.uutishetki"
    compileSdk = 35
    defaultConfig {
        applicationId = "fi.uutishetki"
        minSdk = 26
        targetSdk = 35
        versionCode = version.getProperty("versionCode").toInt()
        versionName = version.getProperty("versionName")
    }
    signingConfigs {
        create("release") {
            storeType = "PKCS12"
            System.getenv("SIGNING_KEYSTORE_PATH")?.let { storeFile = file(it) }
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = false
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = true }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jsoup:jsoup:1.18.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

// Fail closed even when assembleRelease is invoked outside GitHub Actions.
tasks.configureEach {
    if (name == "validateSigningRelease") {
        doFirst {
            val required = listOf("SIGNING_KEYSTORE_PATH", "SIGNING_STORE_PASSWORD", "SIGNING_KEY_ALIAS", "SIGNING_KEY_PASSWORD")
            require(required.all { !System.getenv(it).isNullOrBlank() }) {
                "Release-allekirjoituksen asetuksia puuttuu. Debug-varavaihtoehtoa ei käytetä."
            }
            require(System.getenv("SIGNING_KEY_ALIAS") == "uutishetki-release") { "Väärä julkaisuavainalias." }
            require(file(System.getenv("SIGNING_KEYSTORE_PATH")).isFile) { "Pysyvä release-keystore puuttuu." }
        }
    }
}
