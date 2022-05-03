import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "io.github.kynmarsher"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("com.sparkjava:spark-core:2.9.3")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "io.github.kynmarsher.webserviceback.Application"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}