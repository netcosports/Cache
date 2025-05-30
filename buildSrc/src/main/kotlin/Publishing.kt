import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension

fun Project.publishing(
    configure: PublishingExtension.() -> Unit
): Unit = (this as ExtensionAware).extensions.configure("publishing", configure)

fun Project.signing(
    configure: Action<SigningExtension>
): Unit = (this as ExtensionAware).extensions.configure("signing", configure)

fun Project.initPublishing(
    artifactId: String,
    javadoc: TaskProvider<*>,
) {
    plugins.apply("signing")
    plugins.apply("maven-publish")

    val gpgSigningKeyId = gpgSigningKeyId
    val gpgSigningKey = gpgSigningKey
    val gpgSigningPassphrase = gpgSigningPassphrase
    val useSigning = gpgSigningKeyId.isNotBlank() && gpgSigningKey.isNotBlank() && gpgSigningPassphrase.isNotBlank()
    if (useSigning) {
        val signingTasks = tasks.withType<Sign>(Sign::class.java)
        tasks.withType<AbstractPublishToMaven>(AbstractPublishToMaven::class.java).configureEach {
            dependsOn(signingTasks)
        }
    }

    publishing {
        if (useSigning) {
            signing {
                useInMemoryPgpKeys(
                    /* defaultKeyId = */ gpgSigningKeyId,
                    /* defaultSecretKey = */ gpgSigningKey,
                    /* defaultPassword = */ gpgSigningPassphrase
                )
                sign(publications)
            }
        }

        repositories {
            // use this instead of MavenLocal to generate md5/sha1/sha256/sha512 for MavenCentral
//            maven {
//                this.name = "test"
//                this.url = uri(File("/Users/woffka/.m2/test/"))
//            }
            maven {
                this.name = "OSSRH"
                this.url = uri("https://central.sonatype.com/api/v1/publisher/upload")
                this.credentials {
                    this.username = sonatypeUsername
                    this.password = sonatypePassword
                }
            }
        }
    }

    afterEvaluate {
        publishing {
            publications.forEach { targetPublication ->
                if (targetPublication is MavenPublication) {
                    targetPublication.artifact(javadoc.get())

                    targetPublication.artifactId =
                        targetPublication.artifactId.replace(project.name, artifactId)
                    targetPublication.groupId = Config.Publishing.cacheGroupId
                    targetPublication.version = Config.Publishing.cacheVersion

                    targetPublication.pom {
                        this.name.set("Cache")
                        this.description.set("KMM Cache library")
                        this.url.set("https://github.com/netcosports/Cache")

                        this.organization {
                            this.name.set("ORIGINS Digital")
                            this.url.set("https://github.com/netcosports")
                        }

                        this.licenses {
                            this.license {
                                this.name.set("The Apache License, Version 2.0")
                                this.distribution.set("repo")
                                this.url.set("https://github.com/netcosports/Cache/blob/main/LICENSE")
                            }
                        }

                        this.developers {
                            this.developer {
                                this.id.set("Woffkaa")
                                this.name.set("Vladimir Garkovich")
                                this.email.set("vladimir.garkovich@gmail.com")
                                this.url.set("https://github.com/Woffkaa")
                                this.organization.set("ORIGINS Digital")
                                this.organizationUrl.set("https://github.com/netcosports")
                            }
                        }

                        this.scm {
                            this.url.set("https://github.com/netcosports/Cache")
                            this.connection.set("scm:git:git://github.com/netcosports/Cache.git")
                            this.developerConnection.set("scm:git:git://github.com/netcosports/Cache.git")
                        }
                    }
                }
            }
        }
    }
}