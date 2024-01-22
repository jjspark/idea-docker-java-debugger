plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "ca.justinpark.build.docker-java-debugger"
version = if (version != "unspecified") version else "DEVELOPER-SNAPSHOT"


repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2023.3")
    type.set("IC") // Target IDE Platform
    updateSinceUntilBuild.set(false)

    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("com.github.docker-java:docker-java:3.3.4")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("com.miglayout:miglayout-swing:11.3")
    implementation("com.google.guava:guava:33.0.0-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        if (name == "compileJava") {
            options.compilerArgs.addAll(listOf("-Werror", "-Xlint:all"))
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
    }

    wrapper {
        gradleVersion = "8.5"
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

configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-databind:2.16.1")
        force("commons-codec:commons-codec:1.16.0")
        force("io.netty:netty-codec-http:4.1.106.Final")
        force("io.netty:netty-codec:4.1.106.Final")
        force("io.netty:netty-common:4.1.106.Final")
        force("io.netty:netty-handler:4.1.106.Final")
        force("org.apache.httpcomponents:httpclient:4.5.14")
        force("org.glassfish.jersey.connectors:jersey-apache-connector:2.41")
    }
}
