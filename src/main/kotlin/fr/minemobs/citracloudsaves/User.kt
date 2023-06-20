package fr.minemobs.citracloudsaves

import com.auth0.jwt.interfaces.DecodedJWT
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.conversions.Bson

data class User(val username: String, val password: String) {
    fun filters(): Bson = Filters.and(Filters.eq("username", this.username), Filters.eq("password", this.password))
    fun toDocument() : Document {
        val doc = Document()
        doc["username"] = this.username
        doc["password"] = this.password
        return doc
    }

    companion object {
        fun fromJWT(jwt: DecodedJWT) = User(jwt.getClaim("username").asString(), jwt.getClaim("password").asString())
        fun fromDocument(doc : Document) : User = User(doc["username"].toString(), doc["password"].toString())
    }
}