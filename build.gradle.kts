
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import dev.remodded.regradle.*
import org.jetbrains.dokka.gradle.DokkaPlugin
import java.util.*

plugins {
    kotlin("jvm") version "2.0.0"
    application
    `maven-publish`
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.github.goooler.shadow") version "8.1.7"
    id("dev.remodded.regradle") version "1.0.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

subprojects {
    apply<MavenPublishPlugin>()
    apply<DokkaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply<ShadowPlugin>()

    val props = getPluginProps()

    group = props.group
    version = props.version
    description = props.description

    val dokkaOutputDir = project.layout.buildDirectory.get().dir("dokka")

    val javadocJar by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaHtml)
        archiveClassifier.set("javadoc")
        archiveAppendix.set(project.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        archiveBaseName.set(props.name)
        from(dokkaOutputDir)
    }

    val sourceJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        archiveAppendix.set(project.getProjectSuffix())
        archiveBaseName.set(props.name)
        from(sourceSets.main.get().allSource)
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks {
        dokkaHtml {
            outputDirectory.set(file(dokkaOutputDir))
        }

        assemble {
            dependsOn(shadowJar)
        }

        jar {
            if (needsShadow()) {
                archiveClassifier.set("clean")
            }
        }

        shadowJar {
            if (!needsShadow()) {
                enabled = false
            }
            archiveClassifier.set("")
            archiveAppendix.set(project.getProjectSuffix())
            archiveBaseName.set(props.name)

            dependencies {
                include(project::includeInJar)
            }

            if (isBuildTarget())
                destinationDirectory.set(rootProject.layout.buildDirectory.get().dir("libs"))
        }

        publish {
            dependsOn(publishToMavenLocal)
        }
    }

    repositories {
        maven("https://repo.remodded.dev/repository/maven-central/")
        maven("https://repo.remodded.dev/repository/Mojang/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
    }

    publishing {
        publications {
            create<MavenPublication>(project.name) {
                val username: String? by project
                val password: String? by project

                afterEvaluate {
                    groupId = props.group + "." + props.id
                    artifactId = props.name + "-" + project.getProjectSuffix()

                    from(components["java"])
                    artifact(javadocJar.get())
                    artifact(sourceJar.get())
                }
                repositories {
                    maven {
                        name = "ReModded"
                        url = uri("https://repo.remodded.dev/repository/maven-snapshots/")
                        credentials {
                            this.username = username
                            this.password = password
                        }
                    }
                }
            }
        }
    }
}

afterEvaluate {
    tasks.build.get().setDependsOn(subprojects.map { it.tasks.build })
    tasks.clean.get().setDependsOn(subprojects.map { it.tasks.clean })
}

tasks {
    test {
        useJUnitPlatform()
    }
}
