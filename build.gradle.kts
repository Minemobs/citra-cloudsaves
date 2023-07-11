import com.github.gradle.node.npm.task.NpmTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.node-gradle.node") version "5.0.0"

    application
    eclipse
}

node {
    nodeProjectDir.set(File(project.projectDir, "src/main/resources"))
}

tasks.register("run-dev", NpmTask::class) {
    this.args.set(arrayListOf("run", "build"))
}

tasks.processResources {
    dependsOn(tasks.npmInstall, tasks.getByName("run-dev"))
    filesNotMatching(arrayListOf("dist/*")) { this.exclude() }
    exclude(".vscode", "node_modules", "src/")
}

repositories {
    mavenCentral()
}

dependencies {
    //HTTP
    implementation("io.javalin:javalin:5.6.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    //DB
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
    implementation("com.google.code.gson:gson:2.10.1")
    //Auth
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    //Tests
    testImplementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.9.0")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("fr.minemobs.citracloudsaves.AppKt")
}
