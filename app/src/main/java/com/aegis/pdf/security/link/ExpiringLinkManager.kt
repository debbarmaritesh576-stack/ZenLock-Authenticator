package com.aegis.pdf.core.security

import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiringLinkManager @Inject constructor() {

    private val links = ConcurrentHashMap<String, ExpiringLink>()

    data class ExpiringLink(
        val filePath: String,
        val createdAt: Long,
        val expiryMinutes: Int,
        val password: String?
    ) {
        fun isExpired(): Boolean {
            val expiryTime = createdAt + TimeUnit.MINUTES.toMillis(expiryMinutes.toLong())
            return System.currentTimeMillis() > expiryTime
        }

        fun getRemainingTimeMinutes(): Long {
            val remaining = createdAt + TimeUnit.MINUTES.toMillis(expiryMinutes.toLong()) - System.currentTimeMillis()
            return maxOf(0, TimeUnit.MILLISECONDS.toMinutes(remaining))
        }
    }

    fun createLink(
        file: File,
        expiryMinutes: Int = 60,
        password: String? = null
    ): String {
        val id = UUID.randomUUID().toString().take(8)
        links[id] = ExpiringLink(
            filePath = file.absolutePath,
            createdAt = System.currentTimeMillis(),
            expiryMinutes = expiryMinutes,
            password = password
        )
        return id
    }

    fun getLink(id: String): ExpiringLink? {
        val link = links[id]
        link?.let {
            if (it.isExpired()) {
                links.remove(id)
                return null
            }
        }
        return link
    }

    fun removeLink(id: String) {
        links.remove(id)
    }

    fun cleanExpiredLinks() {
        links.entries.removeIf { it.value.isExpired() }
    }

    fun getActiveLinksCount(): Int {
        cleanExpiredLinks()
        return links.size
    }
}