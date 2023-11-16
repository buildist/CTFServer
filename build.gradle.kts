plugins {
    id("application")
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    implementation(libs.mina)
    implementation(libs.xstream)
    implementation(libs.javacord)
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))

    testImplementation(libs.junit)

    runtimeOnly(libs.slf4jSimple)
}

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
