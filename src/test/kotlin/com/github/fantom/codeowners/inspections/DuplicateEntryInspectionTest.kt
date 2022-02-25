
package com.github.fantom.codeowners.inspections

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.codeInspection.CodeownersDuplicateEntryInspection

class DuplicateEntryInspectionTest : InspectionTestCase() {

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        myFixture.enableInspections(CodeownersDuplicateEntryInspection::class.java)
    }

    @Throws(Exception::class)
    fun testSimpleCase() {
        doHighlightingFileTest()
    }

    @Throws(Exception::class)
    fun testSimpleCaseWithQuickFix() {
        doHighlightingFileTestWithQuickFix(CodeownersBundle.message("quick.fix.remove.entry"))
    }
}
