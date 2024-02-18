plugins {
    id("application")
    id("java-library")
    kotlin("jvm") version libs.versions.kotlin.get()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.guava)
    implementation(libs.mina)
    implementation(libs.xstream)
    implementation(libs.jskills)
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))

    testImplementation(libs.junit)

    runtimeOnly(libs.slf4jSimple)
    // Javacord dependencies below

    // OkHttp for REST-calls
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // the JSON-lib because Discord returns in JSON format
    // Update when upgrading to Java 11+
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.7.1")

    // the web socket
    implementation("com.neovisionaries:nv-websocket-client:2.14")

    // voice encryption
    implementation("com.codahale:xsalsa20poly1305:0.11.0")

    // logging
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")

    // Vavr, mainly for immutable collections
    // We are using 0.10.1, because of an issue in 0.10.2: https://github.com/vavr-io/vavr/issues/2573
    implementation("io.vavr:vavr:0.10.4")

    // For old @Generated annotation in Java 9
    // can be replaced by javax.annotation.processing.Generated if Java 9 is minimum requirement
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")}

val mainClassName = "org.opencraft.server.Server"

application {
    mainClass.set(mainClassName)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude("META-INF/*.RSA", "META-INF/*.sf", "META-INF/*.DSA")

        manifest {
            attributes["Main-Class"] = mainClassName
        }

        from(configurations.runtimeClasspath.get().map { file ->
            if (file.isDirectory) file else zipTree(file)
        })
    }
}
