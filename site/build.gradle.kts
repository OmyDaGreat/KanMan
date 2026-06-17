import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.kobweb)
}

group = "xyz.malefic.kanman"
version = "1.0.0"

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use(::load)
    }

kobweb {
    app {
        index {
            description.set("Powered by Kobweb")
        }
    }
}

kotlin {
    configAsKobwebApplication("kanman")

    jvmToolchain(25)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JVM_25)
                    freeCompilerArgs.add("-Xcontext-parameters")
                    optIn.add("kotlin.uuid.ExperimentalUuidApi")
                }
            }
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("xyz.malefic.kanman.KanManKt")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform(libs.http4k.bom))
            implementation(libs.bundles.http4k)
            implementation(libs.bundles.storage)
            implementation(libs.bcrypt)
            compileOnly(libs.kobweb.api)
        }

        jsMain.dependencies {
            implementation(libs.bundles.silk.icons)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.kobweb)
            implementation(libs.kutint)
        }
    }
}

val jvmJar = tasks.named<Jar>("jvmJar")
val dockerRuntime =
    tasks.register<Copy>("dockerRuntime") {
        description = "Prepares the application for Docker by copying the necessary files into a build directory."

        dependsOn(jvmJar)
        dependsOn("compileProductionExecutableKotlinJs")
        dependsOn("jsBrowserDistribution")

        into(layout.buildDirectory.dir("docker"))

        from(jvmJar) {
            rename { "app.jar" }
        }

        from(configurations.getByName("jvmRuntimeClasspath")) {
            into("lib")
        }

        from(layout.buildDirectory.dir("dist/js/productionExecutable")) {
            into("site/build/dist/js/productionExecutable")
        }
    }

tasks.named("build") {
    dependsOn(dockerRuntime)
}

afterEvaluate {
    afterEvaluate {
        tasks.named<JavaExec>("jvmRun") {
            dependsOn(dockerRuntime)
            systemProperty("API_KEY", localProperties["API_KEY"] ?: System.getenv("API_KEY") ?: "")
        }
    }
}
