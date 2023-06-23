package fr.minemobs.citracloudsaves

import fr.minemobs.citracloudsaves.JWTUtils.getAlgorithm
import fr.minemobs.citracloudsaves.User.Companion.fromJWT
import fr.minemobs.citracloudsaves.JWTUtils.getToken
import fr.minemobs.citracloudsaves.JWTUtils.decodeAndVerify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestJWT {
    @Test
    fun testJWT() {
        val algorithm = getAlgorithm()
        val testUser = User("test", "test")
        val token = getToken(algorithm, testUser)
        val decodedJWT = decodeAndVerify(token, algorithm)
        assertNotNull(decodedJWT)
        assertEquals(testUser, fromJWT(decodedJWT))
    }
}