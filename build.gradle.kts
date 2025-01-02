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
    implementation("com.google.protobuf:protobuf-java:4.29.2")
    implementation("com.google.protobuf:protobuf-java-util:4.29.2")
    implementation("com.google.protobuf:protobuf-kotlin:4.29.2")

    implementation("io.netty:netty-all:4.1.116.Final")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.reflections:reflections:0.9.12")
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
    // from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

kotlin {
    jvmToolchain(21)
}