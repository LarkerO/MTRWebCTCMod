plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val modVersion: String by rootProject.extra
val archivesBaseName: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())
    
    modImplementation("net.fabricmc:fabric-loader:0.14.23")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.76.0+1.18.2")
    
    // Architectury API
    implementation(files(rootProject.file("checkouts/architectury-13.0.8.jar")))
    
    // MTR
    modCompileOnly(files(rootProject.file("libs/mtr3/MTR-fabric-1.18.2-3.2.2-hotfix-1-slim.jar")))
    
    // Common module
    implementation(project(":common"))
}

loom {
    silentMojangMappingsLicense()
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
    
    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "mc_version" to "1.18.2",
            "archives_base_name" to archivesBaseName
        )
    }
}

tasks.named<Jar>("shadowJar") {
    archiveBaseName.set("$archivesBaseName-fabric-1.18.2")
    archiveClassifier.set("")
}
