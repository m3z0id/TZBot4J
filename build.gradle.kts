plugins {
    id("java")
}

group = "com.m3z0id.tzbot4j"
version = "1.0"
description = "TZBot Java library"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.slf4j:slf4j-api:2.0.17")

    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.26.1")

    implementation("com.intellij:annotations:12.0")
}

tasks {
    compileJava {
        options.release = 21
    }
}