import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.5.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    // gradle-grammar-kit-plugin - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2021.2.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

sourceSets["main"].java.srcDirs("src/main/gen")

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.arrow-kt:arrow-core:1.1.2")
}

val generateGithubLexer = task<GenerateLexerTask>("generateGithubLexer") {
    source.set("src/main/grammars/github/CodeownersLexer.flex")
    targetDir.set("src/main/gen/com/github/fantom/codeowners/lang/kind/github/lexer")
    targetClass.set("CodeownersLexer")
    purgeOldFiles.set(true)
}

val generateGithubParser = task<GenerateParserTask>("generateGithubParser") {
    source.set("src/main/grammars/github/Codeowners.bnf")
    targetRoot.set("src/main/gen")
    pathToParser.set("/com/github/fantom/codeowners/lang/kind/github/parser/CodeownersParser.java")
    pathToPsiRoot.set("/com/github/fantom/codeowners/lang/kind/github/psi")
    purgeOldFiles.set(true)
}

val generateBitbucketLexer = task<GenerateLexerTask>("generateBitbucketLexer") {
    source.set("src/main/grammars/bitbucket/CodeownersLexer.flex")
    targetDir.set("src/main/gen/com/github/fantom/codeowners/lang/kind/bitbucket/lexer")
    targetClass.set("CodeownersLexer")
    purgeOldFiles.set(true)
}

val generateBitbucketParser = task<GenerateParserTask>("generateBitbucketParser") {
    source.set("src/main/grammars/bitbucket/Codeowners.bnf")
    targetRoot.set("src/main/gen")
    pathToParser.set("/com/github/fantom/codeowners/lang/kind/bitbucket/parser/CodeownersParser.java")
    pathToPsiRoot.set("/com/github/fantom/codeowners/lang/kind/bitbucket/psi")
    purgeOldFiles.set(true)
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
    headerParserRegex.set("\\[?v(\\d(?:\\.\\d+)+)]?.*".toRegex())
    header.set(
        provider {
            "[v${version.get()}](https://github.com/fan-tom/intellij-codeowners/tree/v${version.get()}) (${date()})"
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

        reports {
            html.required.set(false)
            xml.required.set(false)
            txt.required.set(false)
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
                changelog.run {
                    getOrNull(properties("pluginVersion")) ?: getLatest()
                }.toHTML()
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
