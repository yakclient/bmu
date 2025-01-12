package dev.extframework.archives.mixin.test

import dev.extframework.archives.mixin.*
import dev.extframework.archives.transform.ByteCodeUtils
import dev.extframework.archives.transform.Sources
import dev.extframework.archives.transform.TransformerConfig
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import kotlin.math.floor

class TestSourceMixins {
    @Test
    fun `Test Source accumulation`() {
        println(Sources.of(MixedClass::testMethod))
    }

    private fun getConfig(): TransformerConfig = SourceInjection.apply(
        SourceInjectionData(
            MixedClass::class.java.name,
            `Mixin test case`::class.java.name,
            Sources.of(`Mixin test case`::`Inject this!`),
            ByteCodeUtils.runtimeSignature(MixedClass::testMethod),
            SourceInjectors.AFTER_BEGIN
        )
    )


    @Test
    fun `Test basic configuration creation`() {
        val mixinOf = getConfig()
        println(mixinOf)
    }


    @Test
    fun `Test Bytecode modification with Mixins#resolve`() {
        val config = getConfig()

        val c = MixedClass::class.java.transform(config)

        val instance = c.noArgInstance()
        println(c.getMethod("testMethod", Int::class.java).also(Method::trySetAccessible).invoke(instance, 1))
    }
}

private class MixedClass {
    private var value = "string of something"

    fun testMethod(first: Int): String {
        println(value)
        println("happens second")

        println(first)

        if (System.currentTimeMillis() > 0) println("hey")

        val integer = 5

        val o = listOf("Something", "somethign else")

        for (s in o) {
            println(s)
        }

        if (integer == 3) return "ITS 5!!!"

        for (i in 1..integer) {
            println("SOMETHING")
        }

        return "All finished here"
    }
}

private abstract class `Mixin test case` {
    private var value = ""

    fun `Inject this!`() {
        var a = ""
        println("Injected")

        repeat(floor(Math.random() * 10).toInt()) {
            a += "a"
        }

        println(a)
    }
}