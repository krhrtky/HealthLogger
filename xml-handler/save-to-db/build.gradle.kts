import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(project(":entities"))
    implementation(project(":csv-converter"))
    implementation(project(":file"))
    implementation(project(":dynamo-db-connector"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    jar {

        manifest {
            attributes["Main-Class"] = "MainKt"
        }

        from(
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory) it else zipTree(it)
            }
        )
    }
}

repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
