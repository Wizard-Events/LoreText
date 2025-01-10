plugins {
    java
    id("com.gradleup.shadow") version ("8.3.5")
}

group = "ron.thewizard"
version = "1.0.0"
description = "Set item lores to that of a raw yaml text file"

repositories {
    mavenCentral()

    maven("https://repo.purpurmc.org/snapshots") {
        name = "purpurmc-repo"
    }

    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.21.3-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    build.configure {
        dependsOn("shadowJar")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "name" to project.name,
                    "version" to project.version,
                    "description" to project.description!!.replace('"'.toString(), "\\\""),
                    "url" to "https://github.com/xGinko"
                )
            )
        }
    }

    shadowJar {
        archiveFileName.set("LoreText-${version}.jar")
    }
}