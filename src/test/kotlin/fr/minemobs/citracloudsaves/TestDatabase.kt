package fr.minemobs.citracloudsaves

import fr.minemobs.citracloudsaves.MongoConnection.createMongoClient
import fr.minemobs.citracloudsaves.User.Companion.fromDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestDatabase {
    @Test
    fun testDatabase() {
        val config = App.getConfig() ?: throw NullPointerException("config == null")
        val client = createMongoClient(config)
        val db = client.getDatabase(config.database)
        val coll = db.getCollection(config.collection)
        val user = User("test", "test")
        if (!coll.insertOne(user.toDocument()).wasAcknowledged()) {
            client.close()
            throw RuntimeException("Didn't get acknowledge")
        }
        val nUser = coll.findOneAndDelete(user.filters())
        assertNotNull(nUser, "The user received from the DB is null") {
            client.close()
        }
        assertEquals(user, fromDocument(nUser))
        client.close()
    }
}