rootProject.name = "Cache"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":cache:core")
include(":cache:ktx")
include(":cache:rx")

include(":cache:okhttp:data")

include(":cache:retrofit:data")
include(":cache:retrofit:processor")

include(":cache:ktor:data")

include(":shared")

include(":sample")