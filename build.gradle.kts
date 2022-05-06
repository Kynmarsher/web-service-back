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
    // либа для читаемых uuid
    implementation("com.devskiller.friendly-id:friendly-id:1.1.0")
    implementation("com.devskiller.friendly-id:friendly-id-jackson-datatype:1.1.0")
    // конвертировать объекты в JSON и обратно
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
    implementation("io.socket:socket.io-server:4.0.1")

}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "io.github.kynmarsher.webserviceback.Application"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}