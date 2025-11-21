plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.9"
    id("org.cadixdev.licenser") version "0.6.1"
}

group = "dev.vatten"

repositories {
    mavenCentral()
}

dependencies {

}

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "org.cadixdev.licenser")

    group = rootProject.group
    val pluginId = rootProject.name
    val pluginName = rootProject.property("pluginName")
    version = rootProject.version

    repositories {
        mavenCentral()

        maven {
            url = uri("https://repo.extendedclip.com/releases/")
        }

        maven {
            name = "inventive-repo"
            url = uri("https://repo.inventivetalent.org/repository/public/")
        }
    }

    dependencies {
        implementation("de.exlll:configlib-yaml:4.6.3")
        implementation("org.mineskin:java-client-jsoup:3.2.1-SNAPSHOT")
//        implementation(files("../libs/adventure-text-minimessage.jar"))
//        implementation("net.kyori:adventure-text-serializer-gson:4.25.0")
        compileOnly("net.kyori:adventure-api:4.25.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
        compileOnly("me.clip:placeholderapi:2.11.6")

        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
    }

    artifacts {
        archives(tasks.shadowJar)
    }

    tasks.shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${pluginName}-${version}.jar")
        minimize()
    }

    license {
        header.set(resources.text.fromFile(rootProject.file("HEADER")))
        properties {
            set("name", "vatten <vatten.dev>")
            set("year", 2025)
        }
    }

    tasks.processResources {
        filesMatching("*") {
            expand(
                "pluginId" to pluginId,
                "pluginName" to pluginName,
                "version" to version)
        }
    }
}