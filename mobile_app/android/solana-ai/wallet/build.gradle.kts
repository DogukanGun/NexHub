import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.dag.wallet"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val coinGeckoApiKey = System.getenv("COIN_GECKO_KEY") ?: project.findProperty("COIN_GECKO_KEY")?.toString() ?: "\"\""
        buildConfigField("String", "COIN_GECKO_KEY", "\"$coinGeckoApiKey\"")
        val alloraToolKey = System.getenv("ALLORA_API_KEY") ?: project.findProperty("ALLORA_API_KEY")?.toString() ?: "\"\""
        buildConfigField("String", "ALLORA_API_KEY", "\"$alloraToolKey\"")
        val elfaAiKey = System.getenv("ELFA_AI_API_KEY") ?: project.findProperty("ELFA_AI_API_KEY")?.toString() ?: "\"\""
        buildConfigField("String", "ELFA_AI_API_KEY", "\"$elfaAiKey\"")
        val heliusKey = System.getenv("HELIUS_API_KEY") ?: project.findProperty("HELIUS_API_KEY")?.toString() ?: "\"\""
        buildConfigField("String", "HELIUS_API_KEY", "\"$heliusKey\"")
        val messariKey = System.getenv("MESSARI_API_KEY") ?: project.findProperty("MESSARI_API_KEY")?.toString() ?: "\"\""
        buildConfigField("String", "MESSARI_API_KEY", "\"$messariKey\"")
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    // Proto datastore
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.kotlinx.serialization.json)

    // Biometric
    implementation(libs.androidx.biometric)

    // Solana
    implementation(libs.lazysodium.android)
    implementation(libs.solana)
    implementation(libs.solanakt)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.1"
    }
    generateProtoTasks {

        all().forEach { task ->

            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}