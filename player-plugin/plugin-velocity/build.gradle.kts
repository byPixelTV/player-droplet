dependencies {
    compileOnly(rootProject.libs.velocity)
    api(rootProject.libs.cloud.velocity)
    api(rootProject.libs.cloud.core)
    api(project(":player-shared"))
    api(project(":player-plugin:plugin-shared"))
    api(project(":player-api"))
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
            relocate("org.incendo.cloud", "app.simplecloud.droplet.player.external")
            relocate("io.grpc", "app.simplecloud.relocate.grpc")
            relocate("com.google.protobuf", "app.simplecloud.relocate.protobuf")
        }
    }
}
