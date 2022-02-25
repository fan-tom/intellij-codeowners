
package com.github.fantom.codeowners.inspections

import com.github.fantom.codeowners.CodeownersBundle
import com.github.fantom.codeowners.codeInspection.CodeownersRelativeEntryInspection

class RelativeEntryInspectionTest : InspectionTestCase() {

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        myFixture.enableInspections(CodeownersRelativeEntryInspection::class.java)
    }

    @Throws(Exception::class)
    fun testSimpleCase() {
        doHighlightingFileTest()
    }

    @Throws(Exception::class)
    fun testQuickFix() {
        val name = getTestName(true)
        for (i in 1..5) {
            myFixture.apply {
                configureByCodeownersFile(name + i + FILENAME)
                testHighlighting(true, false, true)
                launchAction(findSingleIntention(CodeownersBundle.message("quick.fix.relative.entry")))
                checkResultByFile("$name$i-after$FILENAME")
            }
        }
    }
}
