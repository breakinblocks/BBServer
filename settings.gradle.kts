pluginManagement {
    repositories {
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net")
            content {
                includeGroupByRegex("de\\.oceanlabs(?:\\..*)?")
                includeGroupByRegex("net\\.minecraft(?:\\..*)?")
                includeGroupByRegex("net\\.minecraftforge(?:\\..*)?")
            }
        }
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
            content {
                includeGroupByRegex("org\\.parchmentmc(?:\\..*)?")
            }
        }
        gradlePluginPortal {
            // Gradle plugin portal includes jcenter, migrate away from this if possible
            content {
                includeGroup("com.github.ben-manes")
                includeGroup("com.github.ben-manes.versions")
                includeGroup("com.gradle.publish")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "bbserver"
