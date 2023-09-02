package fr.minemobs.citracloudsaves

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.BCrypt.Version
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import fr.minemobs.citracloudsaves.Error.Companion.badRequestResponse
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.bson.Document

object RequestUtils {

    @JvmSynthetic
    private val HASHER: BCrypt.Hasher = BCrypt.with(LongPasswordStrategies.truncate(Version.VERSION_2A))
    @JvmSynthetic
    private val VERIFYER: BCrypt.Verifyer = BCrypt.verifyer(Version.VERSION_2A, LongPasswordStrategies.truncate(Version.VERSION_2A))

    fun verifyPassword(ctx: Context, user: Document) = VERIFYER.verifyStrict(ctx.header("password")!!.toCharArray(), user.getString("password").toCharArray()).verified

    @Throws(BadRequestResponse::class)
    fun getUser(ctx: Context) : User {
        val username = ctx.header("username") ?: throw badRequestResponse(Error.USERNAME_HEADER_MISSING)
        val password = ctx.header("password") ?: throw badRequestResponse(Error.PASSWORD_HEADER_MISSING)
        if(!username.matches(Regex("[a-zA-Z_-]\\w{7,20}"))) throw badRequestResponse(Error.INVALID_USERNAME)
        if(!password.matches(Regex("[a-zA-Z0-9@\$!%]{8,20}"))) throw badRequestResponse(Error.INVALID_PASSWORD)
        return User(username, HASHER.hashToString(10, password.toCharArray()))
    }

    @Throws(BadRequestResponse::class)
    fun getAuthorizationToken(ctx: Context) : String {
        val authorization = ctx.header("Authorization") ?: throw badRequestResponse(Error.MISSING_AUTHORIZATION_HEADER)
        val bearer = authorization.split(" ")
        if (bearer.size != 2 || bearer[0] != "Bearer") throw badRequestResponse(Error.INVALID_AUTHORIZATION_HEADER)
        return bearer[1]
    }

    fun toSuccessfulResponse(content: String) = "{\"message\": \"$content\"}"
}