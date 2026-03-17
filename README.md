# Ktor REST API

A Kotlin-based REST API built with Ktor that provides general utility endpoints, an in-memory CRUD API for items, and a proxy to the JSONPlaceholder API for posts.

## Features

- 🚀 Built with Ktor 3.1.3 and Kotlin 2.3.0
- 📦 In-memory data store for items (thread-safe CRUD operations)
- 🌐 JSONPlaceholder API integration for posts
- 📝 JSON serialization with kotlinx.serialization
- 🔍 Request logging with CallLogging
- 🐳 Docker support

## Getting Started

### Prerequisites

- Java 25 (JDK)
- Gradle (wrapper included)

### Running the Application

**Using Gradle:**
```bash
./gradlew run
```

**Using Docker:**
```bash
docker build -t ktor-api .
docker run -p 8080:8080 ktor-api
```

**Building a Fat JAR:**
```bash
./gradlew fatJar
java -jar build/libs/untitled1-1.0-SNAPSHOT-all.jar
```

The server will start on `http://localhost:8080`

## API Endpoints

### General Endpoints

#### `GET /`
Returns a simple welcome message.

**Response:**
```
Hello from Ktor
```

---

#### `GET /health`
Health check endpoint to verify the service is running.

**Response:**
```json
{
  "status": "ok"
}
```

---

#### `GET /hello/{name}`
Personalized greeting endpoint.

**Parameters:**
- `name` (path parameter, required) - The name to greet

**Response (success):**
```json
{
  "message": "Hello, John!"
}
```

**Response (missing name - 400):**
```json
{
  "message": "Missing name"
}
```

---

### Items Endpoints

In-memory CRUD API for managing items. All data is stored in memory and will be lost when the server restarts.

#### `GET /items`
Retrieve all items.

**Response:**
```json
[
  {
    "id": 1,
    "name": "First item"
  },
  {
    "id": 2,
    "name": "Second item"
  }
]
```

---

#### `POST /items`
Create a new item.

**Request Body:**
```json
{
  "name": "My new item"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "My new item"
}
```

---

#### `PUT /items/{id}`
Update an existing item by ID.

**Parameters:**
- `id` (path parameter, required) - The item ID to update

**Request Body:**
```json
{
  "name": "Updated item name"
}
```

**Response (success):**
```json
{
  "id": 1,
  "name": "Updated item name"
}
```

**Response (not found - 404):**
```json
{
  "message": "Item not found"
}
```

**Response (invalid id - 400):**
```json
{
  "message": "Invalid id"
}
```

---

#### `DELETE /items/{id}`
Delete an item by ID.

**Parameters:**
- `id` (path parameter, required) - The item ID to delete

**Response (success - 204):**
```
(empty response)
```

**Response (not found - 404):**
```json
{
  "message": "Item not found"
}
```

---

### Posts Endpoints

Proxy endpoints to the JSONPlaceholder API (https://jsonplaceholder.typicode.com).

#### `GET /posts`
Retrieve all posts from JSONPlaceholder.

**Response:**
```json
[
  {
    "userId": 1,
    "id": 1,
    "title": "sunt aut facere repellat provident",
    "body": "quia et suscipit..."
  },
  ...
]
```

---

#### `GET /posts/{id}`
Retrieve a specific post by ID from JSONPlaceholder.

**Parameters:**
- `id` (path parameter, required) - The post ID to retrieve

**Response (success):**
```json
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident",
  "body": "quia et suscipit..."
}
```

**Response (not found - 404):**
```json
{
  "message": "Post not found"
}
```

---

## Testing

Run the test suite:
```bash
./gradlew test
```

View test reports:
```bash
open build/reports/tests/test/index.html
```

## Project Structure

```
src/
├── main/
│   ├── kotlin/com/example/
│   │   ├── Main.kt          # Application entry point
│   │   └── AppModule.kt     # Ktor module with routes and logic
│   └── resources/
│       └── logback.xml      # Logging configuration
└── test/
    └── kotlin/com/example/
        └── MainTest.kt      # Unit tests
```

## Technologies Used

- **Ktor** - Kotlin web framework
- **Kotlin Serialization** - JSON serialization/deserialization
- **Netty** - Asynchronous event-driven network application framework
- **Logback** - Logging framework
- **CIO** - Coroutine-based I/O for HTTP client
- **Gradle** - Build automation

## License

This project is available under the MIT License.
