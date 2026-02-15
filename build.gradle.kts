plugins {
    id("java")
    `maven-publish`
}

group = "com.m3z0id.tzbot4j"
version = "1.0.13"
description = "TZBot Java library"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    testImplementation("org.msgpack:jackson-dataformat-msgpack:0.9.10")
    testImplementation("com.fasterxml.jackson.core:jackson-core:2.20.1")

    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.slf4j:slf4j-api:2.0.17")

    compileOnly("net.kyori:adventure-api:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.26.1")

    compileOnly("org.msgpack:jackson-dataformat-msgpack:0.9.10")
    compileOnly("com.fasterxml.jackson.core:jackson-core:2.20.1")

    implementation("com.intellij:annotations:12.0")
}

tasks {
    compileJava {
        options.release = 21
    }
}

publishing {
    repositories {
        maven {
            name = "mezoidmvn"
            url = uri("https://mezoidmvn.arcator.co.uk/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.m3z0id.tzbot4j"
            artifactId = "TZBot4J"
            version = version
            from(components["java"])
        }
    }
}

tasks.test {
    // useJUnitPlatform()
}