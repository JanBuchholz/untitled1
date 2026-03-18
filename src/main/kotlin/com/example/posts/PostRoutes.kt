package com.example.posts

import com.example.common.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Configure routing for posts endpoints.
 * Provides read-only access to posts from JSONPlaceholder API.
 */
fun Route.postsRoutes(httpClient: HttpClient) {
    route("/posts") {
        get {
            val posts = httpClient.get("https://jsonplaceholder.typicode.com/posts").body<List<Post>>()
            call.respond(posts)
        }

        get("/{id}") {
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
