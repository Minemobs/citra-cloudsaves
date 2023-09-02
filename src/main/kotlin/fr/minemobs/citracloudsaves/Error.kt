package fr.minemobs.citracloudsaves

import io.javalin.http.BadRequestResponse
import io.javalin.http.HttpStatus
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse

enum class Error(private val text: String) {

    INVALID_USERNAME("Invalid username"),
    INVALID_PASSWORD("Invalid password"),
    INVALID_TOKEN("Invalid token"),
    INVALID_AUTH("Wrong username or password"),
    INVALID_TITLE_ID("Your TitleID is invalid"),
    USERNAME_HEADER_MISSING("Your request is missing the 'username' header"),
    PASSWORD_HEADER_MISSING("Your request is missing the 'password' header"),
    MISSING_AUTHORIZATION_HEADER("Missing the authorization header"),
    INVALID_AUTHORIZATION_HEADER("Invalid authorization header"),
    USER_ALREADY_EXISTS("User already exists"),
    REQUEST_MISSING_SAVE_FILE("Missing the save file in the request"),
    COULD_NOT_FIND_YOUR_SAVE_FILE("Nahh mate, we couldn't find ur save"),
    SAVE_TOO_LARGE("Your save file is too large, if you really are trying to upload a save file, please open an issue here https://github.com/Minemobs/citra-cloudsaves/issues/new");

    private fun toJson() = mapOf("code" to this.ordinal.toString(), "message" to this.text)
    companion object {
        fun badRequestResponse(err: Error) = BadRequestResponse(HttpStatus.BAD_REQUEST.message, err.toJson())
        fun unauthorizedResponse(err: Error) = UnauthorizedResponse(HttpStatus.UNAUTHORIZED.message, err.toJson())
        fun notFoundResponse(err: Error) = NotFoundResponse(HttpStatus.NOT_FOUND.message, err.toJson())
    }
}
