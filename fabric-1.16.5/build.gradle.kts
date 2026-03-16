plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by rootProject.extra
val archivesBaseName: String by rootProject.extra
val ktorVersion: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings(loom.officialMojangMappings())
    
    modImplementation("net.fabricmc:fabric-loader:0.14.23")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.42.0+1.16")
    
    // Architectury API
    implementation(files(rootProject.file("checkouts/architectury-1.32.66.jar")))
    
    // MTR
    modCompileOnly(files(rootProject.file("libs/mtr3/MTR-fabric-1.16.5-3.2.2-hotfix-1-slim.jar")))
    
    // Common module
    implementation(project(":common"))
}

loom {
    silentMojangMappingsLicense()
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
    
    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "mc_version" to "1.16.5",
            "archives_base_name" to archivesBaseName
        )
    }
}

tasks.named<Jar>("shadowJar") {
    archiveBaseName.set("$archivesBaseName-fabric-1.16.5")
    archiveClassifier.set("")
}
