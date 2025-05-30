import org.gradle.api.Project

val Project.gpgSigningKey: String get() = getPropertyByName("gpgSigningKey")
val Project.gpgSigningKeyId: String get() = getPropertyByName("gpgSigningKeyIddd")
val Project.gpgSigningPassphrase: String get() = getPropertyByName("gpgSigningPassphrase")
val Project.sonatypeUsername: String get() = getPropertyByName("sonatypeUsername")
val Project.sonatypePassword: String get() = getPropertyByName("sonatypePassword")

private fun Project.getPropertyByName(name: String): String {
    return properties[name]?.toString() ?: ""
}

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