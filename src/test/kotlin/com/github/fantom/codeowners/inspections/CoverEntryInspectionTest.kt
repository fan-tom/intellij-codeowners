
package com.github.fantom.codeowners.inspections

import com.github.fantom.codeowners.codeInspection.CodeownersCoverEntryInspection

class CoverEntryInspectionTest : InspectionTestCase() {

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        myFixture.enableInspections(CodeownersCoverEntryInspection::class.java)
    }

    @Throws(Exception::class)
    fun testEmptyEntries() {
        doHighlightingTest()
    }

    @Throws(Exception::class)
    fun testDuplicates() {
        doHighlightingTest()
    }

    @Throws(Exception::class)
    fun testCovering() {
        doHighlightingTest()
    }
}
