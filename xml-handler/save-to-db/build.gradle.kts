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
    implementation(project(":s3-connector"))
    implementation(project(":dynamo-db-connector"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.slf4j:slf4j-api:1+")
    implementation("ch.qos.logback:logback-classic:1+")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
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

