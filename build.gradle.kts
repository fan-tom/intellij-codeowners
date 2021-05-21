import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.7.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.1.2"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.17.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("org.jetbrains.grammarkit") version "2021.1.3"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

sourceSets["main"].java.srcDirs("src/main/gen")

// Configure project's dependencies
repositories {
    mavenCentral()
    jcenter()
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
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    setPlugins(*platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}

changelog {
    headerParserRegex = "\\[?v\\d(\\.\\d+)+\\]?.*".toRegex()
    header = closure {
        "[v$version] (https://github.com/fan-tom/intellij-codeowners/tree/v$version) (${org.jetbrains.changelog.date()})"
    }
    version = pluginVersion
    groups = emptyList()
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
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"

        dependsOn(generateGithubLexer, generateGithubParser, generateBitbucketLexer, generateBitbucketParser)
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
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
            closure {
                File(projectDir, "./README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(
            closure {
                changelog.getLatest().toHTML()
            }
        )
    }

    runPluginVerifier {
        ideVersions(pluginVerifierIdeVersions)
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}