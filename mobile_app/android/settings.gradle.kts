import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.mavenCentral

include(":launchpad")
include(":evm")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            val properties = java.util.Properties()
            // Load local.properties.
            properties.load(File(rootDir.absolutePath + "/local.properties").inputStream())

            url = uri("https://maven.pkg.github.com/circlefin/w3s-android-sdk")
            credentials {
                username = properties.getProperty("pwsdk.maven.username")
                password = properties.getProperty("pwsdk.maven.password")
            }
        }
    }
}

rootProject.name = "NexWallet"
include(":app")
include(":solana-ai")
include(":app:aiagent")
include(":solana-ai:wallet")
include(":solana-ai:agent")
