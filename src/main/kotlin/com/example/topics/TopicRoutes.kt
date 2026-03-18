package com.example.topics

import com.example.common.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Configure routing for topics endpoints.
 * Provides operations for topics using an in-memory store.
 */
fun Route.topicsRoutes() {
    val store = TopicStore()
    
    route("/topics") {
        get {
            call.respond(store.all())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid id"))
                return@get
            }

            val topic = store.get(id)
            if (topic == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Topic not found"))
                return@get
            }

            call.respond(topic)
        }

        post {
            val topics = call.receive<List<Topic>>()
            
            // Write received topics to console
            println("Received topics:")
            topics.forEach { topic ->
                println("  - ID: ${topic.id}, Topic: ${topic.topic}")
            }
            
            store.addAll(topics)
            call.respond(HttpStatusCode.Created, topics)
        }
    }
}
