plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mineskin:java-client-jsoup:3.2.1-SNAPSHOT")
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}