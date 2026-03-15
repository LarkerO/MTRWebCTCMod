import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.api.file.RegularFile

plugins {
    id("dev.architectury.loom") version "1.6.422" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("base")
}

val architecturyVersion: String by project
val architecturyVersion1165: String by project
val architecturyRelocationBase = "cn.bg7qvu.mtrwebctc.shaded.architectury"
val ktorRelocationBase = "cn.bg7qvu.mtrwebctc.shaded.ktor"
val checkoutsDir = layout.projectDirectory.dir("checkouts")

// Architectury jar download tasks
val architecturyJarFileName = "architectury-$architecturyVersion.jar"
val architecturyJar = layout.projectDirectory.file("checkouts/$architecturyJarFileName")

val downloadArchitecturyJar = tasks.register("downloadArchitecturyJar") {
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

val downloadArchitecturyJar1165 = tasks.register("downloadArchitecturyJar1165") {
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

// Minecraft target configurations
data class McTarget(
    val minecraftVersion: String,
    val forgeVersion: String,
    val fabricLoaderVersion: String,
    val fabricApiVersion: String,
    val javaVersion: Int,
    val packFormat: Int
)

enum class LoaderType { FABRIC, FORGE }

data class LoaderProject(
    val name: String,
    val loader: LoaderType,
    val target: McTarget
)

val supportedTargets = mapOf(
    "1.16.5" to McTarget(
        minecraftVersion = "1.16.5",
        forgeVersion = "1.16.5-36.2.39",
        fabricLoaderVersion = "0.14.23",
        fabricApiVersion = "0.42.0+1.16",
        javaVersion = 8,
        packFormat = 6
    ),
    "1.18.2" to McTarget(
        minecraftVersion = "1.18.2",
        forgeVersion = "1.18.2-40.2.21",
        fabricLoaderVersion = "0.14.23",
        fabricApiVersion = "0.76.0+1.18.2",
        javaVersion = 17,
        packFormat = 8
    ),
    "1.20.1" to McTarget(
        minecraftVersion = "1.20.1",
        forgeVersion = "1.20.1-47.1.3",
        fabricLoaderVersion = "0.15.10",
        fabricApiVersion = "0.92.1+1.20.1",
        javaVersion = 17,
        packFormat = 15
    )
)

// MTR jar mappings
val mtrLibDir = layout.projectDirectory.dir("libs/mtr3")
val mtrJarNameMap = mapOf(
    "1.16.5" to mapOf(
        LoaderType.FABRIC to "MTR-fabric-1.16.5-3.2.2-hotfix-1-slim.jar",
        LoaderType.FORGE to "MTR-forge-1.16.5-3.2.2-hotfix-1-slim.jar"
    ),
    "1.18.2" to mapOf(
        LoaderType.FABRIC to "MTR-fabric-1.18.2-3.2.2-hotfix-1-slim.jar",
        LoaderType.FORGE to "MTR-forge-1.18.2-3.2.2-hotfix-1-slim.jar"
    ),
    "1.20.1" to mapOf(
        LoaderType.FABRIC to "MTR-fabric-1.20.1-3.2.2-hotfix-1-slim.jar",
        LoaderType.FORGE to "MTR-forge-1.20.1-3.2.2-hotfix-2-slim.jar"
    )
)

val mtrJarMap: Map<String, Map<LoaderType, RegularFile>> = mtrJarNameMap.mapValues { (_, loaders) ->
    loaders.mapValues { (_, fileName) ->
        mtrLibDir.file(fileName)
    }
}

// Loader projects
val loaderProjects = supportedTargets.flatMap { (mcVersion, target) ->
    listOf(
        LoaderProject("fabric-$mcVersion", LoaderType.FABRIC, target),
        LoaderProject("forge-$mcVersion", LoaderType.FORGE, target)
    )
}

// Configure common project
project("common") {
    apply(plugin = "java")
    apply(plugin = "java-library")
    
    val ktorVersion: String by rootProject
    val gsonVersion: String by rootProject
    
    dependencies {
        "compileOnly"(files(architecturyJar1165))
        "compileOnly"(files(mtrJarMap["1.20.1"]!![LoaderType.FABRIC]!!))
        
        // Ktor server
        "implementation"("io.ktor:ktor-server-core:$ktorVersion")
        "implementation"("io.ktor:ktor-server-netty:$ktorVersion")
        "implementation"("io.ktor:ktor-server-websockets:$ktorVersion")
        "implementation"("io.ktor:ktor-server-content-negotiation:$ktorVersion")
        "implementation"("io.ktor:ktor-serialization-gson:$ktorVersion")
        
        // Gson
        "implementation"("com.google.code.gson:gson:$gsonVersion")
    }
    
    tasks.named<JavaCompile>("compileJava") {
        options.release.set(8)
    }
}

// Configure loader projects
loaderProjects.forEach { loaderProject ->
    project(loaderProject.name) {
        apply(plugin = "dev.architectury.loom")
        apply(plugin = "com.github.johnrengelman.shadow")
        
        val target = loaderProject.target
        val is1165 = target.minecraftVersion == "1.16.5"
        val architecturyFile = if (is1165) architecturyJar1165 else architecturyJar
        val mtrFile = mtrJarMap[target.minecraftVersion]!![loaderProject.loader]!!
        
        extensions.configure<LoomGradleExtensionAPI>("loom") {
            silentMojangMappingsLicense()
        }
        
        dependencies {
            "minecraft"("com.mojang:minecraft:${target.minecraftVersion}")
            "mappings"(if (is1165) "net.fabricmc:yarn:${target.minecraftVersion}+build.1:v2" 
                       else "net.fabricmc:yarn:${target.minecraftVersion}+build.1:v2")
            
            if (loaderProject.loader == LoaderType.FABRIC) {
                "modImplementation"("net.fabricmc:fabric-loader:${target.fabricLoaderVersion}")
                "modImplementation"("net.fabricmc.fabric-api:fabric-api:${target.fabricApiVersion}")
            } else {
                "forge"("net.minecraftforge:forge:${target.forgeVersion}")
            }
            
            // Architectury API (shaded)
            "implementation"(files(architecturyFile))
            
            // MTR (compile only)
            "modCompileOnly"(files(mtrFile))
            
            // Common module
            "implementation"(project(":common"))
        }
        
        tasks.named<JavaCompile>("compileJava") {
            options.release.set(target.javaVersion)
        }
        
        tasks.named<ProcessResources>("processResources") {
            val modVersion: String by rootProject
            val archivesBaseName: String by rootProject
            
            inputs.property("version", modVersion)
            inputs.property("mc_version", target.minecraftVersion)
            
            if (loaderProject.loader == LoaderType.FABRIC) {
                filesMatching("fabric.mod.json") {
                    expand(
                        "version" to modVersion,
                        "mc_version" to target.minecraftVersion,
                        "archives_base_name" to archivesBaseName
                    )
                }
            } else {
                filesMatching("META-INF/mods.toml") {
                    expand(
                        "version" to modVersion,
                        "mc_version" to target.minecraftVersion,
                        "archives_base_name" to archivesBaseName
                    )
                }
            }
        }
        
        tasks.named<ShadowJar>("shadowJar") {
            configurations = listOf(project.configurations.getByName("runtimeClasspath"))
            
            // Relocate dependencies to avoid conflicts
            relocate("io.ktor", "$ktorRelocationBase.io.ktor")
            relocate("io.netty", "$ktorRelocationBase.io.netty")
            relocate("kotlin", "$ktorRelocationBase.kotlin")
            
            if (!is1165) {
                relocate("dev.architectury", architecturyRelocationBase)
            } else {
                relocate("me.shedaniel.architectury", architecturyRelocationBase)
            }
            
            archiveClassifier.set("")
            archiveBaseName.set("${rootProject.properties["archivesBaseName"]}-${loaderProject.name}")
        }
    }
}

// Aggregate build tasks
tasks.register("buildAllTargets") {
    group = "build"
    description = "Build all loader targets"
    
    loaderProjects.forEach { loaderProject ->
        dependsOn(":${loaderProject.name}:shadowJar")
    }
}

tasks.register("downloadAllDependencies") {
    group = "setup"
    description = "Download all required dependencies"
    
    dependsOn(downloadArchitecturyJar)
    dependsOn(downloadArchitecturyJar1165)
}
