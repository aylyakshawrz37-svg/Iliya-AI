package com.iliya.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantCoreTest {
    @Test
    fun acceptsHttpsEndpoint() {
        assertTrue(AssistantCore.isValidHttpsEndpoint("https://example.com/api/chat"))
    }

    @Test
    fun rejectsInsecureOrMalformedEndpoint() {
        assertFalse(AssistantCore.isValidHttpsEndpoint("http://example.com/api/chat"))
        assertFalse(AssistantCore.isValidHttpsEndpoint("javascript:alert(1)"))
        assertFalse(AssistantCore.isValidHttpsEndpoint("not a url"))
    }

    @Test
    fun rejectsEndpointWithEmbeddedCredentials() {
        assertFalse(AssistantCore.isValidHttpsEndpoint("https://user:pass@example.com/api"))
    }

    @Test
    fun sanitizesAndLimitsInput() {
        assertEquals("hello", AssistantCore.cleanUserText("  hello  "))
        assertEquals("abcd", AssistantCore.cleanUserText("abcdef", 4))
        assertEquals("ab", AssistantCore.cleanUserText("a\u0000b"))
    }

    @Test
    fun returnsExpectedLanguageCodes() {
        assertEquals("fa", AssistantCore.languageCode(true))
        assertEquals("en", AssistantCore.languageCode(false))
    }
}
