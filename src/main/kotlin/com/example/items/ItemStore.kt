package com.example.items

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * ItemStore is a simple in-memory data store for "items". 
 * It provides thread-safe CRUD operations.
 */
class ItemStore {
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
