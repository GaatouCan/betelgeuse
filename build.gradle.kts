plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // Protobuf
    implementation("com.google.protobuf:protobuf-java:4.29.3")
    implementation("com.google.protobuf:protobuf-java-util:4.29.3")
    implementation("com.google.protobuf:protobuf-kotlin:4.29.3")

    // Netty
    implementation("io.netty:netty-all:4.1.116.Final")

    // Log4j
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")

    // Reflection
    implementation("org.reflections:reflections:0.9.12")

    // YAML
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    implementation("com.zaxxer:HikariCP:6.2.1")

    implementation("org.jetbrains.exposed:exposed-core:0.59.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.59.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.59.0")

    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:0.59.0")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")  // 包含手动生成的 Java 类文件
        }
        kotlin {
            srcDir("src/main/kotlin")  // 包含手动生成的 Kotlin 类文件
        }
    }
}

application {
    mainClass.set("org.example.ServerKt") // 替换为主类的全限定名
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("") // 替代默认的 "-all"
    manifest {
        attributes["Main-Class"] = "org.example.ServerKt"
    }
}

kotlin {
    jvmToolchain(21)
}