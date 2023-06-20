package fr.minemobs.citracloudsaves

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path

object JWTUtils {

    @JvmSynthetic
    @Throws(IOException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun getPrivateKey() = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Files.readAllBytes(Path(".key.txt"))))

    @JvmSynthetic
    @Throws(IOException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun getPublicKey() = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Files.readAllBytes(Path(".key.pub"))))


    @JvmSynthetic
    @Throws(NoSuchAlgorithmException::class, IllegalArgumentException::class)
    internal fun getAlgorithm() : Algorithm {
        val (privateKeyPath, publicKeyPath) = Pair(Path(".key.txt"), Path(".key.pub"))
        if(Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            return Algorithm.RSA256(getPublicKey() as RSAPublicKey, getPrivateKey() as RSAPrivateKey)
        }
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        Files.write(privateKeyPath, keyPair.private.encoded, StandardOpenOption.CREATE)
        Files.write(publicKeyPath, keyPair.public.encoded, StandardOpenOption.CREATE)
        return Algorithm.RSA256(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
    }

    @Throws(JWTCreationException::class, IllegalArgumentException::class)
    fun getToken(algorithm: Algorithm, user: User) : String {
        return JWT.create()
            .withIssuer("minemobs")
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
            .withClaim("username", user.username)
            .withClaim("password", user.password)
            .sign(algorithm)
    }

    fun verifyToken(algorithm: Algorithm, decodedJWT: DecodedJWT): DecodedJWT? {
        return try {
            JWT.require(algorithm)
                .withIssuer("minemobs")
                .build().verify(decodedJWT)
        } catch (e : Exception) {
            null
        }
    }
}