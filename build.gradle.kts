buildscript {
    repositories {
        maven {
            url 'https://files.minecraftforge.net/maven'
            content {
                includeGroup 'net.minecraftforge'
                includeGroup 'net.minecraftforge.gradle'
            }
        }
        jcenter()
    }
    dependencies {
        classpath group: 'net.minecraftforge.gradle', name: 'ForgeGradle', version: '3.+', changing: true
    }
}

apply plugin: 'net.minecraftforge.gradle'
apply plugin: 'java'
apply plugin: 'idea'

version = "${mod_version}"
group = "com.breakinblocks.bbserver"
archivesBaseName = "bbserver-${mc_version}"

sourceCompatibility = targetCompatibility = compileJava.sourceCompatibility = compileJava.targetCompatibility = JavaVersion.VERSION_1_8

minecraft {
    mappings channel: "${mappings_channel}", version: "${mappings_version}"
    accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')

    runs {
        client {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'
            property 'forge.logging.console.level', 'debug'

            mods {
                bbserver {
                    source sourceSets.main
                }
            }
        }

        server {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'
            property 'forge.logging.console.level', 'debug'

            mods {
                bbserver {
                    source sourceSets.main
                }
            }
        }
    }
}

repositories {
    // JEI
    maven {
        url 'https://dvs1.progwml6.com/files/maven'
        content {
            includeGroup 'mezz.jei'
        }
    }

    // FTB Backups
    maven {
        url 'https://www.cursemaven.com'
        content {
            includeGroup 'curse.maven'
        }
    }
}

dependencies {
    minecraft "net.minecraftforge:forge:${mc_version}-${forge_version}"
    runtimeOnly fg.deobf("mezz.jei:jei-${jei_mc_version}:${jei_version}")

    // Curse Maven Mods
    compileOnly fg.deobf("curse.maven:ftb-backups-314904:${ftb_backups_fileid}")
    runtimeOnly fg.deobf("curse.maven:ftb-backups-314904:${ftb_backups_fileid}")
}

processResources {
    inputs.property 'mod_version', project.mod_version
    inputs.property 'mc_version', project.mc_version
    inputs.property 'forge_version_major', project.forge_version_major

    from(sourceSets.main.resources.srcDirs) {
        include 'META-INF/mods.toml'

        expand(
                'mod_version': "${mod_version}",
                'mc_version': "${mc_version}",
                'forge_version_major': "${forge_version_major}",
        )
    }

    // copy everything else except the mods.toml
    from(sourceSets.main.resources.srcDirs) {
        exclude 'META-INF/mods.toml'
    }
}

jar.finalizedBy('reobfJar')

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}
