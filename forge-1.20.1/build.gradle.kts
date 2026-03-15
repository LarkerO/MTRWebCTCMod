plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by rootProject.extra
val archivesBaseName: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.1:v2")
    
    forge("net.minecraftforge:forge:1.20.1-47.1.3")
    
    // Architectury API
    implementation(files(rootProject.file("checkouts/architectury-13.0.8.jar")))
    
    // MTR
    modCompileOnly(files(rootProject.file("libs/mtr3/MTR-forge-1.20.1-3.2.2-hotfix-2-slim.jar")))
    
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
    inputs.property("mc_version", "1.20.1")
    
    filesMatching("META-INF/mods.toml") {
        expand(
            "version" to modVersion,
            "mc_version" to "1.20.1"
        )
    }
}

tasks.named<Jar>("shadowJar") {
    archiveBaseName.set("$archivesBaseName-forge-1.20.1")
    archiveClassifier.set("")
}
