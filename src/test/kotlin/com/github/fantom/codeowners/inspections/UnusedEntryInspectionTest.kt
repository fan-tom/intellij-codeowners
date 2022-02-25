
package com.github.fantom.codeowners.inspections

import com.github.fantom.codeowners.codeInspection.CodeownersUnusedEntryInspection

class UnusedEntryInspectionTest : InspectionTestCase() {

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        myFixture.enableInspections(CodeownersUnusedEntryInspection::class.java)
    }

    @Throws(Exception::class)
    fun testUnusedFile() {
        doHighlightingTest()
    }

    @Throws(Exception::class)
    fun testUnusedDirectory() {
        doHighlightingTest()
    }
}
