plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.formver") version "0.1.0-SNAPSHOT"
}

group = "jesyspa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin.formver:formver.annotations:0.1.0-SNAPSHOT")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

formver {
    conversionTargetsSelection("all_targets")
}