plugins {
    kotlin("jvm") version "2.2.10"
    application
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.example.MainKt")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
}

// Las comprobaciones son un main ejecutable; no son pruebas JUnit.
tasks.test {
    enabled = false
}

// Verificaciones de estudio sin incorporar un framework adicional.
tasks.register<JavaExec>("verificar") {
    dependsOn("testClasses")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("org.example.VerificacionKt")
}

// Ejecuta todos los escenarios aunque alguno falle, para guiar la práctica.
tasks.register<JavaExec>("diagnosticar") {
    dependsOn("testClasses")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("org.example.DiagnosticoPracticaKt")
}

tasks.named("check") {
    dependsOn("verificar")
}
