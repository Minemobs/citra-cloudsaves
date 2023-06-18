package fr.minemobs.citracloudsaves

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context

object RequestUtils {
    @Throws(BadRequestResponse::class)
    fun getUser(ctx: Context) : User {
        val username = ctx.header("username")
        val password = ctx.header("hashPassword")
        if(username == null) throw BadRequestResponse("Your request is missing the 'username' header")
        if(password == null) throw BadRequestResponse("Your request is missing the 'password' header")
        return User(username, password)
    }

    @Throws(BadRequestResponse::class)
    fun getAuthorizationToken(ctx: Context) : String {
        val authorization = ctx.header("authorization") ?: throw BadRequestResponse("Missin' the authorization header")
        val bearer = authorization.split(" ")
        if (bearer.size != 2 || bearer[0] != "Bearer") throw BadRequestResponse("Wrong authorization header")
        return bearer[1]
    }
}