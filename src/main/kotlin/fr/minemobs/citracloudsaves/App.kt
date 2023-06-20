package fr.minemobs.citracloudsaves

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.minemobs.citracloudsaves.JWTUtils.getAlgorithm
import fr.minemobs.citracloudsaves.JWTUtils.getToken
import fr.minemobs.citracloudsaves.RequestUtils.getUser
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.staticfiles.Location
import io.javalin.http.util.NaiveRateLimit
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

object App {
    @JvmSynthetic
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    @JvmSynthetic
    internal fun getConfig() : MongoConnection.MongoConfig? {
        val path = Path("secrets.json")
        if(Files.notExists(path)) {
            Files.writeString(path,
            """
            {
                "host": "localhost"
                "username": "YOUR_DB_USERNAME",
                "password": "YOUR_DB_PASSWORD",
                "database": "users",
                "collection": "user"
            }
            """.trimIndent(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            return null
        }
        Files.newBufferedReader(path).use {
            val obj = GSON.fromJson(it, JsonObject::class.java)
            return MongoConnection.MongoConfig(
                obj["host"].asString,
                obj["username"].asString,
                obj["password"].asString.toCharArray(),
                obj["database"].asString,
                obj["collection"].asString
            )
        }
    }
}

fun main() {
    val algorithm = getAlgorithm()
    val config = App.getConfig() ?: throw NullPointerException("Couldn't connect to the DB due to the 'secrets.json' secrets not being valid.")
    val mongoClient = MongoConnection.createMongoClient(config)
    val usersDB = mongoClient.getDatabase("users")
    val collection = usersDB.getCollection("user")

    Javalin.create { conf ->
        conf.staticFiles.add {
            it.hostedPath = "/"
            it.directory = "/website"
            it.location = Location.CLASSPATH
            it.precompress = true
        }
    }
        .post("register") {
            NaiveRateLimit.requestPerTimeUnit(it, 1, TimeUnit.MINUTES)
            val user = getUser(it)
            collection.insertOne(user.toDocument())
            val token = getToken(algorithm, user)
            it.status(201).result(token)
        }
        .post("login") {
            NaiveRateLimit.requestPerTimeUnit(it, 3, TimeUnit.MINUTES)
            val tempUser = getUser(it)
            val user = collection.find(tempUser.filters()).firstOrNull() ?: throw NotFoundResponse("Wrong username or password")
            val token = getToken(algorithm, User.fromDocument(user))
            it.result(token)
        }
        .post("save/{gameID}") {
            NaiveRateLimit.requestPerTimeUnit(it, 3, TimeUnit.MINUTES)
            //val token = getAuthorizationToken(it)

            val file = it.uploadedFile("save") ?: throw BadRequestResponse("Missin' the save file")
            file.contentAndClose { content ->
                Files.write(Path(it.pathParam("gameID") + ".save"), content.readBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                it.result("Received ur file!")
            }
        }
        .get("save/{gameID}") {
            NaiveRateLimit.requestPerTimeUnit(it, 3, TimeUnit.MINUTES)
            //val token = getAuthorizationToken(it)

            val path = Path(it.pathParam("gameID") + ".save")
            if(Files.notExists(path)) throw NotFoundResponse("Nahh mate, we couldn't find ur save")
            val bytes = Files.readAllBytes(Path(it.pathParam("gameID") + ".save"))
            it.result(bytes)
        }.start(8080)
    mongoClient.close()
}