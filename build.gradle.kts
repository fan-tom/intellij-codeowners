import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    // Java support
    id("java")
    // Kotlin support
    alias(libs.plugins.kotlin)
    // IntelliJ Platform Gradle Plugin
    // https://github.com/JetBrains/intellij-platform-gradle-plugin
    alias(libs.plugins.intelliJPlatform)
    // Gradle Changelog Plugin
    alias(libs.plugins.changelog)
    // Gradle Qodana Plugin
    alias(libs.plugins.qodana)
    // Gradle Kover Plugin
    alias(libs.plugins.kover)
    // gradle-grammar-kit-plugin - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
    alias(libs.plugins.grammarkit)
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    testImplementation(libs.junit)

    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("dk.brics:automaton:1.12-4")

    intellijPlatform {
        val type = properties("platformType").get()
        val version = properties("platformVersion").get()

        create(IntelliJPlatformType.fromCode(type), version)

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(properties("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

val generateGithubLexer = task<GenerateLexerTask>("generateGithubLexer") {
    sourceFile.set(file("src/main/grammars/github/CodeownersLexer.flex"))
    targetOutputDir.set(file("src/main/gen/com/github/fantom/codeowners/lang/kind/github/lexer"))
    purgeOldFiles.set(true)
}

val generateGithubParser = task<GenerateParserTask>("generateGithubParser") {
    sourceFile.set(file("src/main/grammars/github/Codeowners.bnf"))
    targetRootOutputDir.set(file("src/main/gen"))
    pathToParser.set("/com/github/fantom/codeowners/lang/kind/github/parser/CodeownersParser.java")
    pathToPsiRoot.set("/com/github/fantom/codeowners/lang/kind/github/psi")
    purgeOldFiles.set(true)
}

val generateBitbucketLexer = task<GenerateLexerTask>("generateBitbucketLexer") {
    sourceFile.set(file("src/main/grammars/bitbucket/CodeownersLexer.flex"))
    targetOutputDir.set(file("src/main/gen/com/github/fantom/codeowners/lang/kind/bitbucket/lexer"))
    purgeOldFiles.set(true)
}

val generateBitbucketParser = task<GenerateParserTask>("generateBitbucketParser") {
    sourceFile.set(file("src/main/grammars/bitbucket/Codeowners.bnf"))
    targetRootOutputDir.set(file("src/main/gen"))
    pathToParser.set("/com/github/fantom/codeowners/lang/kind/bitbucket/parser/CodeownersParser.java")
    pathToPsiRoot.set("/com/github/fantom/codeowners/lang/kind/bitbucket/psi")
    purgeOldFiles.set(true)
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    // TODO: Configure signing for publishing the plugin
    //signing {
    //    certificateChain = environment("CERTIFICATE_CHAIN")
    //    privateKey = environment("PRIVATE_KEY")
    //    password = environment("PRIVATE_KEY_PASSWORD")
    //}

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    // this break version rendering, adds square brackets
    // see https://github.com/JetBrains/gradle-changelog-plugin/issues/149
    // repositoryUrl = properties("pluginRepositoryUrl")
    headerParserRegex = "\\[?v(\\d(?:\\.\\d+)+)]?.*".toRegex()
    header = provider {
        "[v${version.get()}](https://github.com/fan-tom/intellij-codeowners/tree/v${version.get()}) (${date()})"
    }
    version = properties("pluginVersion")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            compilerOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers")

            dependsOn(generateGithubLexer, generateGithubParser, generateBitbucketLexer, generateBitbucketParser)
        }
    }

    sourceSets {
        main {
            java.srcDirs("src/main/gen")
        }
    }

    clean {
        delete("src/main/gen")
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}
