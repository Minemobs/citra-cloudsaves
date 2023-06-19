package fr.minemobs.citracloudsaves

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.BCrypt.Version
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context

object RequestUtils {

    private val HASHER: BCrypt.Hasher = BCrypt.with(LongPasswordStrategies.truncate(Version.VERSION_2A))
    private val VERIFYER: BCrypt.Verifyer = BCrypt.verifyer(Version.VERSION_2A, LongPasswordStrategies.truncate(Version.VERSION_2A))

    @Throws(BadRequestResponse::class)
    fun getUser(ctx: Context) : User {
        val username = ctx.header("username") ?: throw BadRequestResponse("Your request is missing the 'username' header")
        val password = ctx.header("password") ?: throw BadRequestResponse("Your request is missing the 'password' header")
        return User(username, HASHER.hashToString(10, password.toCharArray()))
    }

    @Throws(BadRequestResponse::class)
    fun getAuthorizationToken(ctx: Context) : String {
        val authorization = ctx.header("authorization") ?: throw BadRequestResponse("Missin' the authorization header")
        val bearer = authorization.split(" ")
        if (bearer.size != 2 || bearer[0] != "Bearer") throw BadRequestResponse("Wrong authorization header")
        return bearer[1]
    }
}