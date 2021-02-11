import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project

plugins {
    application
    kotlin("jvm")
}

group = "com.healthlogger.normalizer"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenLocal()
    jcenter()
    maven {
        url = uri("https://kotlin.bintray.com/ktor")
    }
}

dependencies {
    implementation(project(":file"))
    implementation(project(":csv-converter"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("org.koin:koin-ktor:2+")
    implementation("org.koin:koin-logger-slf4j:2+")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

val jar: Jar by tasks
jar.manifest.attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
jar.from(
    configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }
)
