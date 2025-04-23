subprojects {
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        api(rootProject.libs.kotlin.coroutines)
        api(rootProject.libs.simplecloud.pubsub)
        api(rootProject.libs.bundles.proto)
    }
}