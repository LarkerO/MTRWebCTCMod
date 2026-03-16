plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by rootProject.extra
val archivesBaseName: String by rootProject.extra

loom {
    silentMojangMappingsLicense()
    forge {
        convertAccessWideners.set(false)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())
    
    add("forge", "net.minecraftforge:forge:1.18.2-40.2.21")
    
    // Architectury API
    implementation(files(rootProject.file("checkouts/architectury-13.0.8.jar")))
    
    // MTR
    modCompileOnly(files(rootProject.file("libs/mtr3/MTR-forge-1.18.2-3.2.2-hotfix-1-slim.jar")))
    
    // Common module
    implementation(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.withType<ProcessResources> {
    inputs.property("version", modVersion)
    inputs.property("mc_version", "1.18.2")
    
    filesMatching("META-INF/mods.toml") {
        expand(
            "version" to modVersion,
            "mc_version" to "1.18.2"
        )
    }
}

tasks.named<Jar>("shadowJar") {
    archiveBaseName.set("$archivesBaseName-forge-1.18.2")
    archiveClassifier.set("")
}
