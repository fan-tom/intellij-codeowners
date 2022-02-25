
package com.github.fantom.codeowners.util

import com.github.fantom.codeowners.Common
import org.junit.Test
import java.lang.reflect.InvocationTargetException

class PropertiesTest : Common<Properties>() {

    @Test
    @Throws(
        InvocationTargetException::class,
        NoSuchMethodException::class,
        InstantiationException::class,
        IllegalAccessException::class
    )
    fun testPrivateConstructor() {
        privateConstructor(Properties::class.java)
    }
}
