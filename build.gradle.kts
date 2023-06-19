plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"

    application
    eclipse
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.6.0")
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.bitbucket.b_c:jose4j:0.9.3")
    implementation("at.favre.lib:bcrypt:0.10.2")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.8.21")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("fr.minemobs.citracloudsaves.AppKt")
}
