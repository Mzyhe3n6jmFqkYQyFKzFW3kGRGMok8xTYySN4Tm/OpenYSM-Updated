import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
}

group = property("maven_group") as String
version = property("mod_version") as String
base.archivesName.set(property("archives_name") as String)

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
    maven("https://maven.architectury.dev/") {
        name = "Architectury"
        content {
            includeGroup("dev.architectury")
        }
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
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    include(libs.fabric.api)

    modImplementation(libs.fabric.language.kotlin)
    include(libs.fabric.language.kotlin)

    modImplementation(libs.architectury.fabric) {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }
    include(libs.architectury.fabric)

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
