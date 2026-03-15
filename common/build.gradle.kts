plugins {
    `java-library`
}

val ktorVersion: String by rootProject
val gsonVersion: String by rootProject

repositories {
    mavenCentral()
}

dependencies {
    // Architectury API (compile only for interfaces)
    compileOnly(files(rootProject.layout.projectDirectory.file("checkouts/architectury-1.32.66.jar")))
    
    // MTR API (compile only)
    compileOnly(files(rootProject.layout.projectDirectory.file("libs/mtr3/MTR-fabric-1.20.1-3.2.2-hotfix-1-slim.jar")))
    
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    
    // Gson
    implementation("com.google.code.gson:gson:$gsonVersion")
    
    // SQLite (optional storage backend)
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
