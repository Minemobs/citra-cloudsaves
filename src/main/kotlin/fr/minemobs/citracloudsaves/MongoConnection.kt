package fr.minemobs.citracloudsaves

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

object MongoConnection {

    data class MongoConfig(val host: String, val username: String, val password: CharArray, val database: String, val collection: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MongoConfig) return false

            if (host != other.host) return false
            if (username != other.username) return false
            if (database != other.database) return false
            if (collection != other.collection) return false
            return password.contentEquals(other.password)
        }

        override fun hashCode(): Int {
            var result = host.hashCode()
            result = 31 * result + database.hashCode()
            result = 31 * result + collection.hashCode()
            result = 31 * result + username.hashCode()
            result = 31 * result + password.contentHashCode()
            return result
        }
    }

    fun createMongoClient(config: MongoConfig) : MongoClient {
        val credential = MongoCredential.createCredential(config.username, "admin", config.password)

        return MongoClients.create(MongoClientSettings.builder().credential(credential).applyToClusterSettings { it.hosts(listOf(
            ServerAddress(config.host, 27017)
        )) }.build())
    }
}