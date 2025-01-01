plugins {
    kotlin("jvm") version "2.0.21"
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

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}