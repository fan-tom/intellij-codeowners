
package com.github.fantom.codeowners.refactoring

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.IOException

class RenameTest : BasePlatformTestCase() {

    override fun isWriteActionRequired() = true

    @Throws(IOException::class)
    fun testRenameFile() {
        myFixture.apply {
            tempDirFixture.findOrCreateDir("dir").createChildData(this, "file.txt")
        }
        doTest("*/fil<caret>e.txt", "newFile.txt", "dir/newFile.txt")
    }

    @Throws(IOException::class)
    fun testRenameDirectory() {
        myFixture.apply {
            tempDirFixture.findOrCreateDir("dir").createChildData(this, "file.txt")
        }
        doTest("di<caret>r/file.txt", "newDir", "newDir/file.txt")
    }

    @Throws(IOException::class)
    fun testRenameInNegationEntry() {
        myFixture.apply {
            tempDirFixture.findOrCreateDir("dir").createChildData(this, "file.txt")
        }
        doTest("!di<caret>r/file.txt", "newDir", "!newDir/file.txt")
    }

    private fun doTest(beforeText: String, newName: String, afterText: String) {
        myFixture.apply {
            configureByText(CodeownersFileType.INSTANCE, beforeText)
            renameElementAtCaret(newName)
            checkResult(afterText)
        }
    }
}
