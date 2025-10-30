import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
    alias(libs.plugins.serialization.plugin)
}

group = "com.example.blogmultiplatform"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Kobweb")
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("blogmultiplatform" , includeServer = true)

    sourceSets {
        commonMain.dependencies {
          // Add shared dependencies between JS and JVM here if building a fullstack app
            implementation(libs.kotlinx.serialization)
            implementation(project(":shared"))
        }

        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(libs.silk.icons.fa)
            // This default template uses built-in SVG icons, but what's available is limited.
            // Uncomment the following if you want access to a large set of font-awesome icons:
            // implementation(libs.silk.icons.fa)
            implementation(libs.kobwebx.markdown)
            implementation(libs.kotlin.bootstrap)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
        jvmMain.dependencies {
            compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
            implementation(libs.mongodb.kotlin.driver)
            implementation(libs.kotlinx.serialization)
            // Ktor server and WebSocket dependencies for signaling server
            implementation("io.ktor:ktor-server-netty:2.3.5")
            implementation("io.ktor:ktor-server-websockets:2.3.5")
            implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
        }
    }
}
