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

