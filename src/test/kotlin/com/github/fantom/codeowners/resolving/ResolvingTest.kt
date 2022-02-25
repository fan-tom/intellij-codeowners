
package com.github.fantom.codeowners.resolving

import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.containers.ContainerUtil
import junit.framework.TestCase
import java.io.IOException

class ResolvingTest : BasePlatformTestCase() {

    override fun isWriteActionRequired() = true

//    fun testSimple() {
//        myFixture.tempDirFixture.createFile("fileName.txt")
//        doTest("fileNa<caret>me.txt", "fileName.txt")
//    }

//    @Throws(IOException::class)
//    fun testNestedDirectory() {
//        myFixture.tempDirFixture.findOrCreateDir("dir").createChildData(this, "fileName.txt")
//        doTest("dir/file<caret>Name.txt", "dir/fileName.txt")
//    }
//
//    @Throws(IOException::class)
//    fun testInHiddenDirectory() {
//        myFixture.tempDirFixture.findOrCreateDir(".hidden").createChildData(this, "fileName.txt")
//        doTest(".hidden/file<caret>Name.txt", ".hidden/fileName.txt")
//    }
//
//    @Throws(IOException::class)
//    fun testGlob() {
//        myFixture.tempDirFixture.findOrCreateDir("regex").apply {
//            createChildData(this, "fileName1.txt")
//            createChildData(this, "fileName2.txt")
//        }
//        doTest("regex/*<caret>.txt", "regex/fileName1.txt", "regex/fileName2.txt")
//    }

    @Throws(IOException::class)
    fun testGlobInParent() {
        myFixture.tempDirFixture.apply {
            findOrCreateDir("glob1").createChildData(this, "fileName.txt")
            findOrCreateDir("glob2").createChildData(this, "fileName.txt")
        }
        doTest("*/file<caret>Name.txt", "glob1/fileName.txt", "glob2/fileName.txt")
    }

//    @Throws(IOException::class)
//    fun testInvalidRegex() {
//        myFixture.tempDirFixture.findOrCreateDir("regex").createChildData(this, "fileName1.txt")
//        doTest("regex/fileN(<caret>.txt")
//    }
//
//    @Throws(IOException::class)
//    fun testNegation() {
//        myFixture.tempDirFixture.createFile("fileName.txt")
//        doTest("!fileNa<caret>me.txt", "fileName.txt")
//    }

    @Throws(IOException::class)
    fun testNested() {
        myFixture.tempDirFixture.apply {
            findOrCreateDir("dir1").createChildData(this, "fileName.txt")
            findOrCreateDir("dir2").createChildData(this, "fileName.txt")
        }
        doTest("file<caret>Name.txt", "dir1/fileName.txt", "dir2/fileName.txt")
    }

    private fun doTest(beforeText: String, vararg expectedResolve: String) {
        myFixture.apply {
            configureByText(CodeownersFileType.INSTANCE, beforeText)

            val reference = getReferenceAtCaretPosition() as PsiPolyVariantReference?
            TestCase.assertNotNull(reference)

            val rootFile = file.containingDirectory.virtualFile
            val resolveResults = reference?.multiResolve(true)
            val actualResolve = ContainerUtil.map(resolveResults) {
                it?.element.let { element ->
                    TestCase.assertNotNull(element)
                    UsefulTestCase.assertInstanceOf(element, PsiFileSystemItem::class.java)
                    val fileSystemItem = element as PsiFileSystemItem?
                    fileSystemItem?.let { file -> VfsUtilCore.getRelativePath(file.virtualFile, rootFile, '/') }
                }
            }

            UsefulTestCase.assertContainsElements(actualResolve, *expectedResolve)
        }
    }
}
