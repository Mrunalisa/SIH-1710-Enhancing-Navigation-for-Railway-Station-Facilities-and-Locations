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
        maven { 
            url = uri("https://www.jitpack.io")
            credentials {
                username = "jp_" + System.getenv("JITPACK_TOKEN") // Required for private repos
            }
        }
    }
}

rootProject.name = "Manzil03"
include(":app")
