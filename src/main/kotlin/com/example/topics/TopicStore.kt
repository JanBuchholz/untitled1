package com.example.topics

import java.util.concurrent.ConcurrentHashMap

/**
 * TopicStore is a simple in-memory data store for "topics". 
 * It provides thread-safe storage operations.
 */
class TopicStore {
    private val topics = ConcurrentHashMap<Int, Topic>()

    fun add(topic: Topic) {
        topics[topic.id] = topic
    }

    fun addAll(topicList: List<Topic>) {
        topicList.forEach { topic ->
            topics[topic.id] = topic
        }
    }

    fun get(id: Int): Topic? {
        return topics[id]
    }

    fun all(): List<Topic> {
        return topics.values.sortedBy { it.id }
    }
}
