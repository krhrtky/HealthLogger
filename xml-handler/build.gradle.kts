import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

allprojects {
    apply(plugin = "kotlin")

    group = "me.user"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("joda-time:joda-time:2+")
        implementation("org.slf4j:slf4j-api:1+")
        implementation("ch.qos.logback:logback-classic:1+")
        testImplementation(kotlin("test-junit5"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    }
}

subprojects {
    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }
}

project(":s3-connector") {
    dependencies {
        implementation(project(":entities"))
        implementation(project(":csv-converter"))
        implementation(project(":dynamo-db-connector"))
        implementation("software.amazon.awssdk:bom:2.+")
        implementation("software.amazon.awssdk:s3:2.+")
        implementation("software.amazon.awssdk:ssm:2.+")
    }
}

project(":csv-converter") {
    dependencies {
        implementation(project(":entities"))
        implementation("com.opencsv:opencsv:5+")
    }
}

tasks.test {
    useJUnitPlatform()
}


