plugins {
    `java-library`

    alias(libs.plugins.shadow) // Shadow for triumph
    alias(libs.plugins.run.paper) // Built in test server using runServer and runMojangMappedServer tasks
    alias(libs.plugins.plugin.yml) // Automatic paper-plugin.yml generation

    eclipse
    idea
}

val mainPackage = "${project.group}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21)) // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    withJavadocJar() // Enable javadoc jar generation
    withSourcesJar() // Enable sources jar generation
}

repositories {
    mavenLocal()

    maven("https://maven-central.storage-download.googleapis.com/maven2") // Maven central default mirror
    maven("https://repo.papermc.io/repository/maven-public/") // Paper repository
    maven("https://repo.fancyplugins.de/releases") // FancyPlugins repository
}

dependencies {
    // Core dependencies
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)
    compileOnly(libs.paper.api)
    paperLibrary(libs.guice)
    compileOnly(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    compileOnly(libs.fancy.holograms)
    implementation(libs.triumph.gui)

    // Testing - Core
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
}

tasks {
    shadowJar {
        relocate("dev.triumphteam.gui", "${mainPackage}.gui") // I know what I said about removing the need for shading but triumph is picky
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))
    }

    javadoc {
        isFailOnError = false
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
        options.overview = "src/main/javadoc/overview.html"
        options.windowTitle = "${rootProject.name} Javadoc"
        options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
        options.addStringOption("Xdoclint:none", "-quiet")
        options.use()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    test {
        useJUnitPlatform()
        failFast = false
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion("1.21.10")

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-DIReallyKnowWhatIAmDoingISwear", "-Dpaper.playerconnection.keepalive=6000")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}

paper { // https://github.com/eldoriarpg/plugin-yml
    // Plugin main classes (required)
    main = "${mainPackage}.${project.name}Plugin"
    loader = "${mainPackage}.${project.name}Loader"
    bootstrapper = "${mainPackage}.${project.name}Bootstrapper"
    generateLibrariesJson = true

    // Plugin Information
    name = project.name
    prefix = project.name
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("Bert Towne")
    apiVersion = "1.21.10"
    foliaSupported = true

    serverDependencies {
        register("FancyHolograms") {
            required = true
        }
    }
}