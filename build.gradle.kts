import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.1.4"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.0"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("org.jetbrains.grammarkit") version "2021.1.3"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

sourceSets["main"].java.srcDirs("src/main/gen")

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.1")
    implementation(kotlin("stdlib-jdk8"))
}

val generateGithubLexer = task<GenerateLexer>("generateGithubLexer") {
    source = "src/main/grammars/github/CodeownersLexer.flex"
    targetDir = "src/main/gen/com/github/fantom/codeowners/lang/kind/github/lexer"
    targetClass = "CodeownersLexer"
    purgeOldFiles = true
}

val generateGithubParser = task<GenerateParser>("generateGithubParser") {
    source = "src/main/grammars/github/Codeowners.bnf"
    targetRoot = "src/main/gen"
    pathToParser = "/com/github/fantom/codeowners/lang/kind/github/parser/CodeownersParser.java"
    pathToPsiRoot = "/com/github/fantom/codeowners/lang/kind/github/psi"
    purgeOldFiles = true
}

val generateBitbucketLexer = task<GenerateLexer>("generateBitbucketLexer") {
    source = "src/main/grammars/bitbucket/CodeownersLexer.flex"
    targetDir = "src/main/gen/com/github/fantom/codeowners/lang/kind/bitbucket/lexer"
    targetClass = "CodeownersLexer"
    purgeOldFiles = true
}

val generateBitbucketParser = task<GenerateParser>("generateBitbucketParser") {
    source = "src/main/grammars/bitbucket/Codeowners.bnf"
    targetRoot = "src/main/gen"
    pathToParser = "/com/github/fantom/codeowners/lang/kind/bitbucket/parser/CodeownersParser.java"
    pathToPsiRoot = "/com/github/fantom/codeowners/lang/kind/bitbucket/psi"
    purgeOldFiles = true
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

changelog {
    headerParserRegex.set("\\[?v\\d(\\.\\d+)+\\]?.*".toRegex())
    header.set(
        provider {
            "[v${version.get()}] (https://github.com/fan-tom/intellij-codeowners/tree/v${version.get()}) (${date()})"
        }
    )
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

tasks {
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "11"
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes")

            dependsOn(generateGithubLexer, generateGithubParser, generateBitbucketLexer, generateBitbucketParser)
        }
    }

    withType<Detekt> {
        jvmTarget = "1.8"
    }

    sourceSets {
        main {
            java.srcDirs("src/main/gen")
        }
    }

    clean {
        delete("src/main/gen")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            File(projectDir, "./README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            provider {
                changelog.getLatest().toHTML()
            }
        )
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
