plugins {
    id("java")
    id("application")
}

group = "mta"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.postgresql:postgresql:42.7.3")
    implementation("dnsjava:dnsjava:3.6.1")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("mta.Main")
}

tasks.test {
    useJUnitPlatform()
}
