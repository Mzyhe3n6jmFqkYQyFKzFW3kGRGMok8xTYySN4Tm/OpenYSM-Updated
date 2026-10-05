@file:Suppress("AvoidDuplicateDependencies")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
}

group = providers.gradleProperty("maven_group").get()
version = "${providers.gradleProperty("version").get()}+${libs.versions.minecraft.get()}"
base.archivesName = providers.gradleProperty("archives_name").get()

repositories {
    flatDir {
        dirs(file("libs"))
    }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") {
        name = "Fuzs"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
        content {
            includeGroup("com.github.OpenYSM")
            includeGroup("com.github.TartaricAlkaline")
        }
    }
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "Sponge"
        content {
            includeGroup("org.spongepowered")
        }
    }
    maven("https://maven.ladysnake.org/releases") {
        name = "Ladysnake"
    }
    maven("https://maven.parchmentmc.org") {
        name = "Parchment"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://maven.blamejared.com/") {
        name = "Blamejared"
    }
    maven("https://maven.shedaniel.me/") {
        name = "Shedaniel"
    }
}

loom {
    val aw = file("src/main/resources/${providers.gradleProperty("mod_id").get()}.aw")
    if (aw.exists())
        accessWidenerPath = aw
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment(
            "org.parchmentmc.data:parchment-${
                libs.versions.minecraft.get()
            }:${libs.versions.parchment.mappings.get()}@zip"
        )
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.language.kotlin)

    modImplementation(libs.forge.config.api.port)
    include(libs.forge.config.api.port)

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    modImplementation(libs.cardinal.components.base)
    modImplementation(libs.cardinal.components.entity)
    include(libs.cardinal.components.base)
    include(libs.cardinal.components.entity)

    implementation(libs.imagestream)
    include(libs.imagestream)

    implementation(libs.concentus)
    include(libs.concentus)

    implementation(libs.vorbis.java.core)
    include(libs.vorbis.java.core)

    implementation(libs.aircompressor)
    include(libs.aircompressor)

    modImplementation(libs.iris)
    modImplementation(libs.touhoulittlemaid.fabric)
    modImplementation(libs.crawl)
    modImplementation(libs.modmenu)
    modImplementation(libs.carryon)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val version = version
    inputs.property("version", version)
    inputs.property("minecraft_version", libs.versions.minecraft.get())
    inputs.property("loader_version", libs.versions.fabric.loader.get())
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "mod_id" to providers.gradleProperty("mod_id").get(),
            "mod_name" to providers.gradleProperty("mod_name").get(),
            "minecraft_version" to libs.versions.minecraft.get(),
            "loader_version" to libs.versions.fabric.loader.get(),
            "fabric_kotlin_version" to libs.versions.fabric.language.kotlin.get(),
            "fabric_api_version" to libs.versions.fabric.api.get(),
            "mod_menu_version" to libs.versions.modmenu.get(),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = libs.versions.jvm.target.get().toInt()
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:none",
            "-Xlint:-deprecation",
            "-Xlint:-removal",
            "-Xlint:-unchecked",
            "-nowarn"
        )
    )
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvm.target.get())
    }
    sourceSets {
        main {
            kotlin.srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
    }
}
