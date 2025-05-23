rootProject.name = "MVP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()  // Remove the mavenContent block
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()  // Remove the mavenContent block
        mavenCentral()
        mavenLocal()
    }
}

include(":composeApp")