plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mineskin:java-client-jsoup:3.2.6")
    implementation(project(":common"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}