import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.mavenCentral

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
    }
}

rootProject.name = "NexWallet"
include(":app")
include(":solana-ai")
include(":app:aiagent")
include(":solana-ai:wallet")
include(":solana-ai:agent")
include(":evm")