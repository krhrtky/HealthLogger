plugins {
    kotlin("jvm")
}
group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    implementation("org.slf4j:slf4j-api:1+")
    implementation("ch.qos.logback:logback-classic:1+")
    implementation("joda-time:joda-time:2+")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.opencsv:opencsv:5+")
    testImplementation(kotlin("test-junit5"))
}
