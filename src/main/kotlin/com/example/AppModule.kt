//*******************************************************************************************
//* Packages and Imports. This section includes all the necessary imports for our 
//* Ktor application, including Ktor server and client libraries, serialization, and logging.
//*******************************************************************************************

package com.example

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


//*******************************************************************************************
//* Application.module() - This is the main entry point for our Ktor application. It sets up 
//* logging, content negotiation, and defines all our API routes.
//*******************************************************************************************
fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        // Keep this concise and stable; logback.xml already routes INFO to the console.
        format { call ->
            "${call.request.httpMethod.value} ${call.request.uri}"
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            },
        )
    }
    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                },
            )
        }
    }

    //*************************************************************************
    //* Routing. This is where we define our API endpoints and their handlers.
    //*************************************************************************
    routing {

        //------------------------------------------------------------------------
        //- General endpoints
        //------------------------------------------------------------------------

        get("/") {
            call.respondText("Hello from Ktor", ContentType.Text.Plain)
        }

        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        get("/hello/{name?}") {
            val name = call.parameters["name"]?.trim().orEmpty()
            if (name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, HelloResponse(message = "Missing name"))
                return@get
            }
            call.respond(HelloResponse(message = "Hello, $name!"))
        }


        //------------------------------------------------------------------------
        //- In-memory CRUD API for "items"
        //------------------------------------------------------------------------
        val store = ItemStore()

        get("/items") {
            call.respond(store.all())
        }

        post("/items") {
            val req = call.receive<CreateItemRequest>()
            val created = store.create(req.name)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid id"))
                return@put
            }

            val req = call.receive<UpdateItemRequest>()
            val updated = store.update(id, req.name)
            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Item not found"))
                return@put
            }

            call.respond(updated)
        }

        delete("/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid id"))
                return@delete
            }

            val deleted = store.delete(id)
            if (!deleted) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Item not found"))
                return@delete
            }

            call.respond(HttpStatusCode.NoContent, "")
        }

        //------------------------------------------------------------------------
        //- JSONPlaceholder API endpoints
        //------------------------------------------------------------------------
        get("/posts") {
            val posts = httpClient.get("https://jsonplaceholder.typicode.com/posts").body<List<Post>>()
            call.respond(posts)
        }

        get("/posts/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid id"))
                return@get
            }

            try {
                val post = httpClient.get("https://jsonplaceholder.typicode.com/posts/$id").body<Post>()
                call.respond(post)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Post not found"))
            }
        }
    }
}


//*******************************************************************************************
//* Serialization. These data classes define the structure of our API requests and responses.
//*******************************************************************************************
@Serializable
data class HealthResponse(val status: String)

@Serializable
data class HelloResponse(val message: String)

@Serializable
data class CreateItemRequest(val name: String)

@Serializable
data class UpdateItemRequest(val name: String)

@Serializable
data class Item(val id: Int, val name: String)

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)


//********************************************************************************************************
//* Class ItemStore is a simple in-memory data store for "items". It provides thread-safe CRUD operations.
//********************************************************************************************************
private class ItemStore {
    private val nextId = AtomicInteger(0)
    private val items = ConcurrentHashMap<Int, Item>()

    fun all(): List<Item> {
        return items.values.sortedBy { it.id }
    }

    fun create(name: String): Item {
        val id = nextId.incrementAndGet()
        val item = Item(id = id, name = name)
        items[id] = item
        return item
    }

    fun update(id: Int, name: String): Item? {
        return items.computeIfPresent(id) { _, existing -> existing.copy(name = name) }
    }

    fun delete(id: Int): Boolean {
        return items.remove(id) != null
    }
}
