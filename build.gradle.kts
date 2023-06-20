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
