import org.eclipse.jgit.api.Git

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)

    alias(libs.plugins.buildConfig)
    alias(libs.plugins.shadow)
}

group = "dev.maerkle"
version = "1.0"

dependencies {
    implementation(platform(libs.ktor.bom))

    implementation(libs.ktor.contentnegotiation.json)
    implementation(libs.ktor.contentnegotiation.server)

    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.statuspages)
    implementation(libs.ktor.auth.core)
    implementation(libs.ktor.auth.jwt)

    implementation(platform(libs.stdx.bom))
    implementation(libs.stdx.core)
    implementation(libs.stdx.envconf)
    implementation(libs.stdx.logging)
    implementation(libs.stdx.coroutines)

    implementation(libs.mongodb)

    implementation(libs.klogger)
    implementation(libs.slf4jSimple)
    implementation(libs.kotlinSerialization)

    implementation(libs.mina.core)
    implementation(libs.mina.sshd)
    implementation(libs.mina.sftp)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.koin.logging)

    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.ktor.client.contentnegotiation)

    testImplementation(libs.kotlinTest)
}

tasks {
    register("runWithEnv", RunWithEnvironmentConfig::class) {
        // https://ktor.io/docs/server-packaging.html#run
        dependsOn("installDist")
        environmentFile.set(File(".env"))
    }

    kotlin {
        jvmToolchain(21)
    }

    test {
        useJUnitPlatform()
    }
}

application {
    mainClass.set("dev.maerkle.stausee.StauseeKt")
}

val versionKey = "VERSION"
val gitShaKey = "GIT_SHA"
val gitBranchKey = "GIT_BRANCH"

buildConfig {
    packageName("${project.group}.${project.name}.generated")

    val git = Git.open(project.rootDir.resolve(".git"))
    val head = git.repository.findRef("HEAD")
    buildConfigField("String?", gitShaKey, "\"${head.objectId.name}\"")
    buildConfigField("String?", gitBranchKey, "\"${git.repository.branch}\"")

    buildConfigField(String::class.java, versionKey, version.toString())
}