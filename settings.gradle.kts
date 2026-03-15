pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        maven("https://maven.architectury.dev") { name = "Architectury" }
        maven("https://files.minecraftforge.net/maven") { name = "Forge" }
    }
}

rootProject.name = "MTRWebCTCMod"

include("common")

// Fabric loaders
include("fabric-1.16.5")
include("fabric-1.18.2")
include("fabric-1.20.1")

// Forge loaders
include("forge-1.16.5")
include("forge-1.18.2")
include("forge-1.20.1")
