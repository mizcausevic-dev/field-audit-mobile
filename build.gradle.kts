plugins {
    kotlin("jvm") version "2.2.20"
    application
}

group = "com.kineticgain"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.kineticgain.fieldaudit.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
