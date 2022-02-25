package com.github.fantom.codeowners.inspections

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.lang.CodeownersLanguage
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ResourceUtil
import java.io.File

@Suppress("UnnecessaryAbstractClass")
abstract class InspectionTestCase : BasePlatformTestCase() {

    companion object {
        val FILENAME = CodeownersLanguage.INSTANCE.filename
    }

    override fun getTestDataPath(): String {
        val url = Thread.currentThread().contextClassLoader.getResource("inspections") ?: return ""
        return File(url.path + "/" + name()).absolutePath
    }

    private fun name() = StringUtil.decapitalize(
        StringUtil.trimEnd(
            StringUtil.trimStart(javaClass.simpleName, "Gitignore"),
            "InspectionTest"
        )
    )

    override fun isWriteActionRequired() = false

    protected fun doHighlightingTest() {
        myFixture.apply {
            copyDirectoryToProject(getTestName(true), getTestName(true))
            testHighlighting(true, false, true, getTestName(true) + "/" + FILENAME)
        }
    }

    protected fun doHighlightingFileTest() {
        myFixture.apply {
            configureByCodeownersFile(getTestName(true) + FILENAME)
            testHighlighting(true, false, true)
        }
    }

    protected fun doHighlightingFileTestWithQuickFix(quickFixName: String) {
        myFixture.apply {
            configureByCodeownersFile(getTestName(true) + FILENAME)
            testHighlighting(true, false, true)
            launchAction(findSingleIntention(quickFixName))
            checkResultByFile("${getTestName(true)}-after$FILENAME")
        }
    }

    protected fun configureByCodeownersFile(fileName: String) {
        val resource = javaClass.classLoader.getResourceAsStream("inspections/${name()}/$fileName") ?: return
        val text = ResourceUtil.loadText(resource)

        myFixture.configureByText(CodeownersFileType.INSTANCE, text)
    }
}
