import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    application
}

allprojects {
    group = "me.user"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {
    subprojects.forEach {
        implementation(it)
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("software.amazon.awssdk:bom:2.+")
    implementation("software.amazon.awssdk:s3:2.+")
    implementation("software.amazon.awssdk:ssm:2.+")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.opencsv:opencsv:5+")
    implementation("joda-time:joda-time:2+")
    implementation("org.slf4j:slf4j-api:1+")
    implementation("ch.qos.logback:logback-classic:1+")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}


tasks.test {
    useJUnitPlatform()
}


