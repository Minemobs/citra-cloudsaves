package fr.minemobs.citracloudsaves

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.minemobs.citracloudsaves.Error.Companion.badRequestResponse
import fr.minemobs.citracloudsaves.Error.Companion.notFoundResponse
import fr.minemobs.citracloudsaves.JWTUtils.getAlgorithm
import fr.minemobs.citracloudsaves.JWTUtils.getToken
import fr.minemobs.citracloudsaves.JWTUtils.initSaveRequest
import fr.minemobs.citracloudsaves.RequestUtils.getUser
import fr.minemobs.citracloudsaves.RequestUtils.toSuccessfulResponse
import fr.minemobs.citracloudsaves.RequestUtils.verifyPassword
import io.javalin.Javalin
import io.javalin.http.ContentType
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
    internal fun getConfig(): MongoConnection.MongoConfig? {
        val path = Path("secrets.json")
        val env = System.getenv()
        //Only need to check one env
        if (Files.notExists(path) && !env.containsKey("MONGO_HOST")) {
            Files.writeString(
                path,
                """
            {
                "host": "localhost",
                "username": "YOUR_DB_USERNAME",
                "password": "YOUR_DB_PASSWORD",
                "database": "citra-cloudsaves",
                "collection": "user"
            }
            """.trimIndent(), StandardOpenOption.CREATE, StandardOpenOption.WRITE
            )
            return null
        } else if(env.containsKey("MONGO_HOST")) {
            val host = env["MONGO_HOST"] ?: return null
            val username = env["MONGO_USERNAME"] ?: return null
            val password = env["MONGO_PASSWORD"] ?: return null
            val database = env["MONGO_DATABASE"] ?: return null
            val collection = env["MONGO_COLLECTION"] ?: return null
            return MongoConnection.MongoConfig(host, username, password.toCharArray(), database, collection)
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
    val saveDir = Path("saves")
    if(Files.notExists(saveDir)) Files.createDirectory(saveDir)
    val algorithm = getAlgorithm()
    val config = App.getConfig()
        ?: throw NullPointerException("Couldn't connect to the DB due to the 'secrets.json' secrets not being valid.")
    val mongoClient = MongoConnection.createMongoClient(config)
    val usersDB = mongoClient.getDatabase(config.database)
    val collection = usersDB.getCollection(config.collection)

    val app = Javalin.create { conf ->
        conf.staticFiles.add {
            it.hostedPath = "/"
            it.directory = "/dist"
            it.location = Location.CLASSPATH
            it.precompress = false
        }
        conf.http.defaultContentType = ContentType.JSON
    }
        .post("register") {
            NaiveRateLimit.requestPerTimeUnit(it, 1, TimeUnit.MINUTES)
            val user = getUser(it)
            if(collection.find(user.filters()).firstOrNull() != null) throw badRequestResponse(Error.USER_ALREADY_EXISTS)
            collection.insertOne(user.toDocument())
            val token = getToken(algorithm, user)
            it.status(201).result(toSuccessfulResponse(token))
        }
        .post("login") {
            NaiveRateLimit.requestPerTimeUnit(it, 3, TimeUnit.MINUTES)
            val tempUser = getUser(it)
            val user = collection.find(tempUser.filters()).firstOrNull()
                ?: throw notFoundResponse(Error.INVALID_AUTH)
            if(!verifyPassword(it, user)) throw notFoundResponse(Error.INVALID_AUTH)
            val token = getToken(algorithm, User.fromDocument(user))
            it.result(toSuccessfulResponse(token))
        }
        .post("save/{gameID}") {
            val (user, gameSaveDir) = initSaveRequest(it, algorithm, saveDir)

            val file = it.uploadedFile("save") ?: throw badRequestResponse(Error.REQUEST_MISSING_SAVE_FILE)
            if(file.size() > 1e6) throw badRequestResponse(Error.SAVE_TOO_LARGE)
            file.contentAndClose { content ->
                Files.write(
                    gameSaveDir.resolve("${user.username}.save"),
                    content.readBytes(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
                )
                it.result(toSuccessfulResponse("Received ur file!"))
            }
        }
        .get("save/{gameID}") {
            val (user, gameSaveDir) = initSaveRequest(it, algorithm, saveDir)
            val path = gameSaveDir.resolve("${user.username}.save")
            if (Files.notExists(path)) throw notFoundResponse(Error.COULD_NOT_FIND_YOUR_SAVE_FILE)
            val bytes = Files.readAllBytes(saveDir.resolve(it.pathParam("gameID")).resolve("${user.username}.save"))
            it.contentType(ContentType.OCTET_STREAM).result(bytes)
        }.delete("save/{gameID}") {
            val (user, gameSaveDir) = initSaveRequest(it, algorithm, saveDir)
            val path = gameSaveDir.resolve("${user.username}.save")
            if (!Files.deleteIfExists(path)) throw notFoundResponse(Error.COULD_NOT_FIND_YOUR_SAVE_FILE)
            it.result(toSuccessfulResponse("Deleted your save"))
        }.events { it.serverStopping { mongoClient.close() } }
    Runtime.getRuntime().addShutdownHook(Thread { app.stop() })
    app.start(8888)
}
