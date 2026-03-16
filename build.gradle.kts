import org.gradle.api.tasks.bundling.Jar
import java.net.URL
import java.io.FileOutputStream

plugins {
    id("java")
    id("dev.architectury.loom") version "1.6.422" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

val architecturyVersion = "13.0.8"
val architecturyVersion1165 = "1.32.66"
val ktorVersion = "2.3.9"
val gsonVersion = "2.10.1"

extra["modVersion"] = "1.0.0"
extra["mavenGroup"] = "cn.bg7qvu.mtrwebctc"
extra["archivesBaseName"] = "mtrwebctc"
extra["ktorVersion"] = ktorVersion

val checkoutsDir = layout.projectDirectory.dir("checkouts")

// Architectury jar download tasks
val architecturyJarFileName = "architectury-$architecturyVersion.jar"
val architecturyJar = layout.projectDirectory.file("checkouts/$architecturyJarFileName")

tasks.register("downloadArchitecturyJar") {
    outputs.file(architecturyJar)
    doLast {
        val targetFile = architecturyJar.asFile
        checkoutsDir.asFile.mkdirs()
        if (!targetFile.exists() || targetFile.length() == 0L) {
            val url = "https://maven.architectury.dev/dev/architectury/architectury/$architecturyVersion/$architecturyJarFileName"
            logger.lifecycle("Downloading Architectury API jar from $url")
            java.net.URL(url).openStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

val architecturyJarFileName1165 = "architectury-$architecturyVersion1165.jar"
val architecturyJar1165 = layout.projectDirectory.file("checkouts/$architecturyJarFileName1165")

tasks.register("downloadArchitecturyJar1165") {
    outputs.file(architecturyJar1165)
    doLast {
        val targetFile = architecturyJar1165.asFile
        checkoutsDir.asFile.mkdirs()
        if (!targetFile.exists() || targetFile.length() == 0L) {
            val url = "https://maven.architectury.dev/me/shedaniel/architectury/$architecturyVersion1165/$architecturyJarFileName1165"
            logger.lifecycle("Downloading Architectury API jar from $url")
            java.net.URL(url).openStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

// Aggregate build task
tasks.register("buildAllTargets") {
    group = "build"
    description = "Build all loader targets"
    
    dependsOn(":fabric-1.16.5:shadowJar")
    dependsOn(":fabric-1.18.2:shadowJar")
    dependsOn(":fabric-1.20.1:shadowJar")
    dependsOn(":forge-1.16.5:shadowJar")
    dependsOn(":forge-1.18.2:shadowJar")
    dependsOn(":forge-1.20.1:shadowJar")
}

tasks.register("downloadAllDependencies") {
    group = "setup"
    description = "Download all required dependencies"
    
    dependsOn("downloadArchitecturyJar")
    dependsOn("downloadArchitecturyJar1165")
}

// Configure allprojects
allprojects {
    apply(plugin = "java")
    
    repositories {
        mavenCentral()
        maven(url = "https://maven.fabricmc.net")
        maven(url = "https://maven.architectury.dev")
        maven(url = "https://files.minecraftforge.net/maven")
    }
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
