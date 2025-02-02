import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonatype.central.portal.publisher)
    `maven-publish`
}

allprojects {
    group = "app.simplecloud.droplet.player"
    version = determineVersion()

    repositories {
        mavenCentral()
        maven("https://buf.build/gen/maven")
        maven("https://repo.simplecloud.app/snapshots/")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "net.thebugmc.gradle.sonatype-central-portal-publisher")
    apply(plugin = "maven-publish")

    dependencies {
        testImplementation(rootProject.libs.kotlin.test)
        implementation(rootProject.libs.kotlin.jvm)
    }

    publishing {
        repositories {
            maven {
                name = "simplecloud"
                url = uri(determineRepositoryUrl())
                credentials {
                    username = System.getenv("SIMPLECLOUD_USERNAME")
                        ?: (project.findProperty("simplecloudUsername") as? String)
                    password = System.getenv("SIMPLECLOUD_PASSWORD")
                        ?: (project.findProperty("simplecloudPassword") as? String)
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }

        publications {
            if (project.name == "player-runtime" || project.name.contains("plugin")) {
                return@publications
            }

            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    kotlin {
        jvmToolchain(21)

        compilerOptions {
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks {
        withType<JavaCompile> {
            options.isFork = true
            options.isIncremental = true
        }

        named("shadowJar", ShadowJar::class) {
            mergeServiceFiles()
            archiveFileName.set("${project.name}.jar")
        }

        test {
            useJUnitPlatform()
        }
    }

    centralPortal {
        name = project.name

        username = project.findProperty("sonatypeUsername") as? String
        password = project.findProperty("sonatypePassword") as? String

        pom {
            name.set("SimpleCloud Player Droplet")
            description.set("The player droplet is an optional but powerful component that provides comprehensive player management, cross-server communication, and advanced interaction features using the Adventure API")
            url.set("https://github.com/simplecloudapp/player-droplet")

            developers {
                developer {
                    id.set("SimpleCloud Developers")
                }
            }
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            scm {
                url.set("https://github.com/simplecloudapp/player-droplet.git")
                connection.set("git:git@github.com:simplecloudapp/player-droplet.git")
            }
        }
    }

    signing {
        val releaseType = project.findProperty("releaseType")?.toString() ?: "snapshot"
        if (releaseType != "release") {
            return@signing
        }

        if (hasProperty("signingPassphrase")) {
            val signingKey: String? by project
            val signingPassphrase: String? by project
            useInMemoryPgpKeys(signingKey, signingPassphrase)
        } else {
            useGpgCmd()
        }

        sign(publishing.publications)
    }
}

fun determineVersion(): String {
    val baseVersion = project.findProperty("baseVersion")?.toString() ?: "0.0.0"
    val releaseType = project.findProperty("releaseType")?.toString() ?: "snapshot"
    val commitHash = System.getenv("COMMIT_HASH") ?: "local"

    return when (releaseType) {
        "release" -> baseVersion
        "rc" -> "$baseVersion-rc.$commitHash"
        "snapshot" -> "$baseVersion-SNAPSHOT.$commitHash"
        else -> "$baseVersion-SNAPSHOT.local"
    }
}

fun determineRepositoryUrl(): String {
    val baseUrl = "https://repo.simplecloud.app/"
    return when (project.findProperty("releaseType")?.toString() ?: "snapshot") {
        "release" -> "$baseUrl/releases"
        "rc" -> "$baseUrl/rc"
        else -> "$baseUrl/snapshots"
    }
}