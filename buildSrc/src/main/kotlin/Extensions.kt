import org.gradle.api.Project

val Project.repoUrl: String get() = "https://maven.pkg.github.com/netcosports/maven-packages"
val Project.repoUsername: String get() = this.properties["ghUsername"].toString()
val Project.repoPassword: String get() = this.properties["ghPassword"].toString()


fun getCustomVersionName(): String {
    return System.getenv("VERSION_NAME") ?: Config.Build.versionName
}

fun getCustomVersionCode(): Int {
    val versionCode = System.getenv("VERSION_CODE")?.toIntOrNull()
    if (versionCode != null) {
        return versionCode
    }
    val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull()
    if (buildNumber != null) {
        return buildNumber + Config.Build.versionOffset
    }
    return 1
}