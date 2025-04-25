dependencies {
    compileOnly(rootProject.libs.paper)

    api(project(":player-shared"))
    api(project(":player-plugin:plugin-shared"))
    api(project(":player-api"))
}


tasks {
    shadowJar {
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
            relocate("io.grpc", "app.simplecloud.relocate.grpc")
            relocate("com.google.protobuf", "app.simplecloud.relocate.protobuf")
        }
    }
}
