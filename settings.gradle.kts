rootProject.name = "Cache_Android"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":cache:core")
include(":cache:core-ktx")
include(":cache:core-rx")

include(":okhttp:cache:data")

include(":retrofit:cache:data")
include(":retrofit:cache:processor")

include(":ktor:cache:data")



include(":shared")
include(":sample")