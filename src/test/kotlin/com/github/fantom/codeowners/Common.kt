
package com.github.fantom.codeowners

import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry
import com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor
import com.github.fantom.codeowners.util.Constants
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

@Suppress("UnnecessaryAbstractClass")
abstract class Common<T> : BasePlatformTestCase() {

    @Throws(
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    protected fun privateConstructor(clz: Class<T>) {
        val constructor = clz.getDeclaredConstructor()
        TestCase.assertTrue(Modifier.isPrivate(constructor.modifiers))
        constructor.isAccessible = true
        constructor.newInstance()
    }

    protected fun createCodeownersContent(vararg entries: String?) = StringUtil.join(entries, Constants.NEWLINE)

    protected val fixtureRootFile
        get() = myFixture.file.containingDirectory.virtualFile

    protected val fixtureChildrenEntries: List<com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry>
        get() {
            val children: MutableList<com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry> = mutableListOf()
            myFixture.file.acceptChildren(
                object : com.github.fantom.codeowners.lang.kind.github.psi.CodeownersVisitor() {
                    override fun visitEntry(entry: com.github.fantom.codeowners.lang.kind.github.psi.CodeownersEntry) {
                        children.add(entry)
                    }
                }
            )
            return children
        }
}
