dependencies {
    compileOnly(rootProject.libs.spigot)
    api(project(":player-shared"))
    api(project(":player-plugin:plugin-shared"))
    api(project(":player-api"))
    api(rootProject.libs.adventure.platform.spigot)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core"))
            relocate("io.grpc", "app.simplecloud.relocate.grpc")
            relocate("com.google.protobuf", "app.simplecloud.relocate.protobuf")
        }
    }
}
