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
            relocate("io.grpc", "app.simplecloud.relocate.grpc")
            relocate("com.google.protobuf", "app.simplecloud.relocate.protobuf")
        }
    }
}
