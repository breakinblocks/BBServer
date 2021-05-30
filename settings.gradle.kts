pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.minecraftforge.gradle" -> {
                    useModule("net.minecraftforge.gradle:ForgeGradle:5.0.1")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://maven.minecraftforge.net")
            content {
                includeGroup("net.minecraft")
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
            }
        }
        gradlePluginPortal {
            // Gradle plugin portal includes jcenter, migrate away from this if possible
            content {
                includeGroup("com.gradle.publish")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "bbserver"
