dependencies {
    compileOnly(rootProject.libs.simplecloud.controller)
    compileOnly(rootProject.libs.cloud.core)
    api(project(":player-shared"))
    api(project(":player-api"))
    api(rootProject.libs.adventure)
    api(rootProject.libs.adventure.text.minimessage)
}