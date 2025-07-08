plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {

    namespace = "com.dag.solai"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":app:aiagent"))
    implementation(project(":solana-ai:wallet"))
    implementation(libs.langchain4j)
    implementation(libs.langchain4j.openai)
    implementation(libs.langchain4j.open.ai)
    implementation(libs.ktor.client.core.v321)
    implementation(libs.ktor.client.cio.v321)
    implementation(libs.kotlinx.serialization.json)
}