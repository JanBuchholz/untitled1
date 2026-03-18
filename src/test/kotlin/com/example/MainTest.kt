package com.example

import com.example.common.HealthResponse
import com.example.items.Item
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

class MainTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun root_returns_text() = testApplication {
        application {
            module()
        }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello from Ktor", response.bodyAsText())
    }

    @Test
    fun health_returns_json() = testApplication {
        application {
            module()
        }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val parsed = json.decodeFromString(HealthResponse.serializer(), body)
        assertEquals("ok", parsed.status)
    }

    @Test
    fun items_crud_post_put_delete() = testApplication {
        application {
            module()
        }

        val emptyListResp = client.get("/items")
        assertEquals(HttpStatusCode.OK, emptyListResp.status)
        val emptyList = json.decodeFromString(ListSerializer(Item.serializer()), emptyListResp.bodyAsText())
        assertTrue(emptyList.isEmpty())

        val createdResp =
            client.post("/items") {
                headers { append(HttpHeaders.ContentType, ContentType.Application.Json.toString()) }
                setBody("""{"name":"first"}""")
            }
        assertEquals(HttpStatusCode.Created, createdResp.status)
        val created = json.decodeFromString(Item.serializer(), createdResp.bodyAsText())
        assertEquals("first", created.name)

        val afterCreateListResp = client.get("/items")
        assertEquals(HttpStatusCode.OK, afterCreateListResp.status)
        val afterCreateList =
            json.decodeFromString(ListSerializer(Item.serializer()), afterCreateListResp.bodyAsText())
        assertEquals(listOf(created), afterCreateList)

        val updatedResp =
            client.put("/items/${created.id}") {
                headers { append(HttpHeaders.ContentType, ContentType.Application.Json.toString()) }
                setBody("""{"name":"second"}""")
            }
        assertEquals(HttpStatusCode.OK, updatedResp.status)
        val updated = json.decodeFromString(Item.serializer(), updatedResp.bodyAsText())
        assertEquals(created.id, updated.id)
        assertEquals("second", updated.name)

        val afterUpdateListResp = client.get("/items")
        assertEquals(HttpStatusCode.OK, afterUpdateListResp.status)
        val afterUpdateList =
            json.decodeFromString(ListSerializer(Item.serializer()), afterUpdateListResp.bodyAsText())
        assertEquals(listOf(updated), afterUpdateList)

        val deletedResp = client.delete("/items/${created.id}")
        assertEquals(HttpStatusCode.NoContent, deletedResp.status)

        val afterDeleteListResp = client.get("/items")
        assertEquals(HttpStatusCode.OK, afterDeleteListResp.status)
        val afterDeleteList =
            json.decodeFromString(ListSerializer(Item.serializer()), afterDeleteListResp.bodyAsText())
        assertTrue(afterDeleteList.isEmpty())

        val deletedAgainResp = client.delete("/items/${created.id}")
        assertEquals(HttpStatusCode.NotFound, deletedAgainResp.status)
    }
}
