plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":csv-converter"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("software.amazon.awssdk:bom:2.+")
    implementation("software.amazon.awssdk:s3:2.+")
    implementation("software.amazon.awssdk:ssm:2.+")
    implementation("org.slf4j:slf4j-api:1+")
    implementation("ch.qos.logback:logback-classic:1+")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}
