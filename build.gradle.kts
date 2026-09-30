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
    include(libs.fabric.language.kotlin)

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

    modCompileOnly(libs.iris)

    modCompileOnly(libs.touhoulittlemaid.fabric)
    modLocalRuntime(libs.touhoulittlemaid.fabric)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(libs.versions.jvm.target.get().toInt())
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
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
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
