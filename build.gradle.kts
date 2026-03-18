plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    application
}

group = "org.example"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "3.1.3"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("com.example.MainKt")
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a runnable jar with all runtime dependencies."

    dependsOn(tasks.named("classes"))

    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isZip64 = true

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Application classes/resources
    from(sourceSets.main.get().output)

    // Runtime dependencies (Ktor, Netty, etc.)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

tasks.test {
    useJUnitPlatform()
}
