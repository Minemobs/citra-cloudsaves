package fr.minemobs.citracloudsaves

import com.auth0.jwt.JWT
import fr.minemobs.citracloudsaves.JWTUtils.getAlgorithm
import fr.minemobs.citracloudsaves.User.Companion.fromJWT
import fr.minemobs.citracloudsaves.JWTUtils.getToken
import fr.minemobs.citracloudsaves.JWTUtils.verifyToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestJWT {
    @Test
    fun testJWT() {
        val algorithm = getAlgorithm()
        val testUser = User("test", "test")
        val token = getToken(algorithm, testUser)
        val decodedJWT = verifyToken(algorithm, JWT.decode(token))
        assertNotNull(decodedJWT)
        assertEquals(testUser, fromJWT(decodedJWT))
    }
}