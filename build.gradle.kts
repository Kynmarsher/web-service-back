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
    // либа для читаемых uuid
    // конвертировать объекты в JSON и обратно
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    // https://mavenlibs.com/maven/dependency/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:1.7.36")
    // https://mavenlibs.com/maven/dependency/com.corundumstudio.socketio/netty-socketio
    implementation("com.corundumstudio.socketio:netty-socketio:1.7.19")

}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "io.github.kynmarsher.webserviceback.Application"
    }
}