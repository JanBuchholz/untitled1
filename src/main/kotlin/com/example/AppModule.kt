//*******************************************************************************************
//* Packages and Imports. This section includes all the necessary imports for our 
//* Ktor application, including Ktor server and client libraries, serialization, and logging.
//*******************************************************************************************

package com.example

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

//Import von andern Modulen
import com.example.common.HealthResponse
import com.example.common.HelloResponse
import com.example.items.itemsRoutes
import com.example.posts.postsRoutes


//*******************************************************************************************
//* Application.module() - This is the main entry point for our Ktor application. It sets up 
//* logging, content negotiation, and defines all our API routes using modular design.
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
    //* Routing. This is where we define our API endpoints using modules.
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
        //- Module-specific routes
        //------------------------------------------------------------------------
        itemsRoutes()
        postsRoutes(httpClient)
    }
}
