package fr.minemobs.citracloudsaves

import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.keys.AesKey
import org.jose4j.lang.ByteUtil
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

object JWTUtils {
    private fun createAndWriteKey(path: Path) : AesKey {
        val bytes = ByteUtil.randomBytes(16)
        Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
        return AesKey(bytes)
    }

    @JvmSynthetic
    internal fun getKey() : AesKey {
        val path = Path(".key.txt")
        if (Files.notExists(path)) {
            return createAndWriteKey(path)
        }
        val bytes = Files.readAllBytes(path)
        return if (bytes.size < 16) createAndWriteKey(path) else AesKey(bytes)
    }

    @JvmSynthetic
    internal fun jweSerialize(key: AesKey, user: User) : String {
        val jwe = JsonWebEncryption()

        jwe.key = key
        jwe.algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.A128KW
        jwe.encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256
        jwe.payload = App.GSON.toJson(user)
        return jwe.compactSerialization
    }

    @Suppress("unused")
    @JvmSynthetic
    internal fun jweDeserialize(key: AesKey, serializedUser: String) : User {
        val jwe = JsonWebEncryption()
        jwe.key = key
        jwe.setAlgorithmConstraints(AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, KeyManagementAlgorithmIdentifiers.A128KW))
        jwe.setContentEncryptionAlgorithmConstraints(AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256))
        jwe.compactSerialization = serializedUser
        return App.GSON.fromJson(jwe.payload, User::class.java)
    }
}