package com.example.items

import com.example.common.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Configure routing for items endpoints.
 * Provides CRUD operations for items using an in-memory store.
 */
fun Route.itemsRoutes() {
    val store = ItemStore()
    
    route("/items") {
        get {
            call.respond(store.all())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid id"))
                return@get
            }

            val item = store.get(id)
            if (item == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Item not found"))
                return@get
            }

            call.respond(item)
        }

        post {
            val req = call.receive<CreateItemRequest>()
            val created = store.create(req.name)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
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

        delete("/{id}") {
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
    }
}
