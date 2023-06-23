package fr.minemobs.citracloudsaves

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import fr.minemobs.citracloudsaves.Error.Companion.badRequestResponse
import fr.minemobs.citracloudsaves.Error.Companion.unauthorizedResponse
import io.javalin.http.Context
import io.javalin.http.util.NaiveRateLimit
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
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
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

object JWTUtils {

    @JvmSynthetic
    @Throws(IOException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun getPrivateKey() = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Files.readAllBytes(Path(".key.txt"))))

    @JvmSynthetic
    @Throws(IOException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun getPublicKey() = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Files.readAllBytes(Path(".key.pub"))))

    @JvmSynthetic
    @Throws(JWTDecodeException::class)
    internal fun decodeAndVerify(token: String, algorithm: Algorithm) = JWT.decode(token).verifyToken(algorithm)

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

    fun DecodedJWT.verifyToken(algorithm: Algorithm): DecodedJWT? {
        return try {
            JWT.require(algorithm)
                .withIssuer("minemobs")
                .build().verify(this)
        } catch (e : Exception) {
            null
        }
    }

    fun initSaveRequest(ctx: Context, algorithm: Algorithm, saveDir: Path) : Pair<User, Path> {
        NaiveRateLimit.requestPerTimeUnit(ctx, 3, TimeUnit.MINUTES)
        if(!ctx.pathParam("gameID").matches(Regex("\\d+"))) throw badRequestResponse(Error.INVALID_GAME_ID)
        val token = decodeAndVerify(RequestUtils.getAuthorizationToken(ctx), algorithm) ?: throw unauthorizedResponse(Error.INVALID_TOKEN)
        val user = User.fromJWT(token)
        val gameSaveDir = saveDir.resolve(ctx.pathParam("gameID"))
        if(Files.notExists(gameSaveDir)) Files.createDirectory(gameSaveDir)
        return user to gameSaveDir
    }
}