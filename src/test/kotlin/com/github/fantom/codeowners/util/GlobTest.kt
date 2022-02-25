
package com.github.fantom.codeowners.util

import com.github.fantom.codeowners.Common
import com.github.fantom.codeowners.file.type.CodeownersFileType
import com.github.fantom.codeowners.services.CodeownersMatcher
import com.intellij.openapi.components.service
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
// import org.junit.Assert
import org.junit.Test
import java.lang.reflect.InvocationTargetException

@Suppress("UnsafeCallOnNullableType")
class GlobTest : Common<Glob>() {

    @Test
    @Throws(
        InvocationTargetException::class,
        NoSuchMethodException::class,
        InstantiationException::class,
        IllegalAccessException::class
    )
    fun testPrivateConstructor() {
        privateConstructor(Glob::class.java)
    }

    @Test
    fun testFind() {
        myFixture.apply {
            configureByText(
                CodeownersFileType.INSTANCE,
                createCodeownersContent("foo.txt @foo", "bar.txt @bar", "buz.txt @buz", "vcsdir @vcs", "dir @dir")
            )
            addFileToProject("bar.txt", "bar content")
            addFileToProject("buz.txt", "buz content")
            addFileToProject("vcsdir/.git/info", "vcsdir git info content")
            addFileToProject("dir/buz.txt", "buz2 content")
            addFileToProject("dir/biz.txt", "buz2 content")
        }
        val dir = fixtureRootFile.findChild("dir")
        TestCase.assertNotNull(dir)

        val matcher = project.service<CodeownersMatcher>()
        var result = Glob.find(fixtureRootFile, fixtureChildrenEntries, matcher, false)

        // foo.txt
        result[fixtureChildrenEntries[0]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertEmpty(it)
        }

        // bar.txt
        result[fixtureChildrenEntries[1]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 1)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("bar.txt")))
        }

        // buz.txt
        result[fixtureChildrenEntries[2]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 2)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("buz.txt")))
            TestCase.assertTrue(it.contains(dir!!.findChild("buz.txt")))
        }

        // ignore VCS directory
        result[fixtureChildrenEntries[3]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 1)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("vcsdir")))
        }

        // dir
        result[fixtureChildrenEntries[4]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 1)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("dir")))

            // dir not includeNested
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 1)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("dir")))
        }

        // dir includeNested
        result = Glob.find(fixtureRootFile, fixtureChildrenEntries, matcher, true)
        result[fixtureChildrenEntries[4]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 3)
            TestCase.assertTrue(it.contains(fixtureRootFile.findChild("dir")))
            TestCase.assertTrue(it.contains(dir?.findChild("buz.txt")))
            TestCase.assertTrue(it.contains(dir?.findChild("biz.txt")))
        }
    }

    @Test
    fun testFindAsPaths() {
        myFixture.apply {
            configureByText(
                CodeownersFileType.INSTANCE,
                createCodeownersContent("foo.txt @foo", "bar.txt @bar", "buz.txt @buz", "vcsdir @vcs", "dir @dir")
            )
            addFileToProject("bar.txt", "bar content")
            addFileToProject("buz.txt", "buz content")
            addFileToProject("vcsdir/.git/info", "vcsdir git info content")
            addFileToProject("dir/buz.txt", "buz2 content")
            addFileToProject("dir/biz.txt", "buz2 content")
        }
        val dir = fixtureRootFile.findChild("dir")
        TestCase.assertNotNull(dir)

        val matcher = project.service<CodeownersMatcher>()
        val result = Glob.findAsPaths(fixtureRootFile, fixtureChildrenEntries, matcher, false)

        // foo.txt
        result[fixtureChildrenEntries[0]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertEmpty(it)
        }

        // bar.txt
        result[fixtureChildrenEntries[1]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 1)
            TestCase.assertTrue(it.contains("bar.txt"))
        }

        // buz.txt
        result[fixtureChildrenEntries[2]]!!.let {
            TestCase.assertNotNull(result)
            UsefulTestCase.assertNotEmpty(it)
            TestCase.assertEquals(it.size, 2)
            TestCase.assertTrue(it.contains("buz.txt"))
            TestCase.assertTrue(it.contains("dir/buz.txt"))
        }
    }

//    @Test
//    @Throws(Exception::class)
//    @Suppress("LongMethod")
//    fun testCreatePattern() {
//        Regex.createPattern("file.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/subdir/file.txt").matches())
//            Assert.assertFalse(it.matcher("file1.txt").matches())
//            Assert.assertFalse(it.matcher("otherfile.txt").matches())
//        }
//
//        Regex.createPattern("file*.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/file.txt").matches())
//        }
//
//        Regex.createPattern("fil[eE].txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("file.txt").matches())
//            TestCase.assertTrue(it.matcher("filE.txt").matches())
//            Assert.assertFalse(it.matcher("fild.txt").matches())
//        }
//
//        Regex.createPattern("dir/file.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("dir/file.txt").matches())
//            Assert.assertFalse(it.matcher("xdir/dir/file.txt").matches())
//            Assert.assertFalse(it.matcher("xdir/file.txt").matches())
//        }
//
//        Regex.createPattern("/file.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("file.txt").matches())
//            Assert.assertFalse(it.matcher("dir/file.txt").matches())
//        }
//
//        Regex.createPattern("fi**le.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("file.txt").matches())
//            TestCase.assertTrue(it.matcher("fi-foo-le.txt").matches())
//            Assert.assertFalse(it.matcher("fi/le.txt").matches())
//            Assert.assertFalse(it.matcher("fi/foo/le.txt").matches())
//        }
//
//        Regex.createPattern("**/dir/file.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("foo/dir/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/file.txt").matches())
//        }
//
//        Regex.createPattern("/dir/**/file.txt").let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("dir/subdir/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/subdir/foo/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/file.txt").matches())
//        }
//
//        Regex.createPattern("subdir", true).let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("dir/subdir/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/subdir/").matches())
//            Assert.assertFalse(it.matcher("dir/foo/bar.txt").matches())
//        }
//
//        Regex.createPattern("subdir/", true).let {
//            TestCase.assertNotNull(it)
//            TestCase.assertTrue(it!!.matcher("dir/subdir/file.txt").matches())
//            TestCase.assertTrue(it.matcher("dir/subdir/").matches())
//            Assert.assertFalse(it.matcher("dir/foo/bar.txt").matches())
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun `test trailing slash star`() {
//        Regex.createPattern("dir/*", false).let {
//            TestCase.assertNotNull(it)
//            // test match file
//            TestCase.assertTrue(it!!.matcher("dir/file.txt").matches())
//            // test don't match folder
//            TestCase.assertFalse(it.matcher("dir/subdir/").matches())
//            // test match only at first level
//            TestCase.assertFalse(it.matcher("dir/subdir/file.txt").matches())
//            // test match only content, not folder itself
//            Assert.assertFalse(it.matcher("dir/").matches())
//        }
//    }
//
//    @Test
//    fun `test create regex`() {
//        // If there is a separator at the beginning or middle (or both) of the pattern,
//        // then the pattern is relative to the directory level of the particular .gitignore file itself.
//        // Otherwise the pattern may also match at any level below the .gitignore level.
//        assertEquals("^(?:[^/]*?/)*file\\.txt/?$", Regex.createRegex("file.txt", false))
//        assertEquals("^docs/[^/]+$", Regex.createRegex("docs/*", false))
//    }
}
