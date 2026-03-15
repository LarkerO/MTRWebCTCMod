plugins {
    id("java")
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    // Architectury API (compile only)
    compileOnly(files(rootProject.file("checkouts/architectury-13.0.8.jar")))
    
    // MTR (compile only)
    compileOnly(files(rootProject.file("libs/mtr3/MTR-fabric-1.20.1-3.2.2-hotfix-1-slim.jar")))
    
    // Ktor server
    implementation("io.ktor:ktor-server-core:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.ktor:ktor-server-websockets:2.3.9")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-gson:2.3.9")
    implementation("io.ktor:ktor-server-cors:2.3.9")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
