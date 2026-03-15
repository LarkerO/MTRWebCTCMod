plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by rootProject.extra
val archivesBaseName: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings("net.fabricmc:yarn:1.16.5+build.1:v2")
    
    forge("net.minecraftforge:forge:1.16.5-36.2.39")
    
    // Architectury API
    implementation(files(rootProject.file("checkouts/architectury-1.32.66.jar")))
    
    // MTR
    modCompileOnly(files(rootProject.file("libs/mtr3/MTR-forge-1.16.5-3.2.2-hotfix-1-slim.jar")))
    
    // Common module
    implementation(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}

tasks.withType<ProcessResources> {
    inputs.property("version", modVersion)
    inputs.property("mc_version", "1.16.5")
    
    filesMatching("META-INF/mods.toml") {
        expand(
            "version" to modVersion,
            "mc_version" to "1.16.5"
        )
    }
}

tasks.named<Jar>("shadowJar") {
    archiveBaseName.set("$archivesBaseName-forge-1.16.5")
    archiveClassifier.set("")
}
