@file:Suppress("PropertyName")

import net.minecraftforge.gradle.userdev.UserDevExtension

val mod_version: String by project
val mc_version: String by project
val mc_version_range_supported: String by project
val forge_version: String by project
val forge_version_range_supported: String by project
val mappings_channel: String by project
val mappings_version: String by project
val jei_mc_version: String by project
val jei_version: String by project
val ftb_backups_fileid: String by project

@Suppress("DSL_SCOPE_VIOLATION") // Workaround for https://youtrack.jetbrains.com/issue/KTIJ-19370.
plugins {
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.forge.gradle.three)
    alias(libs.plugins.parchment.librarian)
}

group = "com.breakinblocks.bbserver"
version = mod_version
base.archivesBaseName = "bbserver-${mc_version}"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<UserDevExtension> {
    mappings(mappings_channel, mappings_version)
    runs {
        create("client") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            mods {
                create("bbserver") {
                    sources = listOf(sourceSets["main"])
                }
            }
        }
        create("server") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")

            mods {
                create("bbserver") {
                    sources = listOf(sourceSets["main"])
                }
            }
        }
    }
}

repositories {
    // JEI
    maven {
        url = uri("https://dvs1.progwml6.com/files/maven")
        content {
            includeGroup("mezz.jei")
        }
    }

    // FTB Backups
    maven {
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    add("minecraft", "net.minecraftforge:forge:${mc_version}-${forge_version}")
    runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}:${jei_version}"))

    // Curse Maven Mods
    compileOnly(fg.deobf("curse.maven:ftb-backups-314904:${ftb_backups_fileid}"))
    runtimeOnly(fg.deobf("curse.maven:ftb-backups-314904:${ftb_backups_fileid}"))
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("mod_version", mod_version)
    inputs.property("mc_version_range_supported", mc_version_range_supported)
    inputs.property("forge_version_range_supported", forge_version_range_supported)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")
        expand(
            "mod_version" to mod_version,
            "mc_version_range_supported" to mc_version_range_supported,
            "forge_version_range_supported" to forge_version_range_supported
        )
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("META-INF/mods.toml")
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Specification-Title" to "BBServer",
            "Specification-Vendor" to "Breakin' Blocks",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Breakin' Blocks"
        )
    }
    finalizedBy("reobfJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
