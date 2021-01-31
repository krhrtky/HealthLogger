plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(project(":csv-converter"))
    implementation(project(":s3-connector"))
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

