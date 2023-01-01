plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.11.0"
}

group = "ca.justinpark.build.docker-java-debugger"
version = if (version != "unspecified") version else "DEVELOPER-SNAPSHOT"


repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2021.3.3")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("com.github.docker-java:docker-java:3.+")
    implementation("org.slf4j:slf4j-api:2.+")
    implementation("com.miglayout:miglayout-swing:11.+")
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.+")
    testImplementation("org.mockito:mockito-core:4.+")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        if (name == "compileJava") {
            options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all"))
        }
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    wrapper {
        gradleVersion = "7.6"
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        useJUnitPlatform()
        maxHeapSize = "1G"
        testLogging {
            events("passed")
        }
    }
}
