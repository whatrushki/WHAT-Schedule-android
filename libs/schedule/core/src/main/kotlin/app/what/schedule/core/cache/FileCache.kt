package app.what.schedule.core.cache

/**
 * Стандартизированный интерфейс кэширования файлов с поддержкой TTL и автоочистки.
 * Не имеет зависимостей от Android SDK и может использоваться на любых платформах.
 */
interface FileCache {
    /**
     * Получить байты файла по ключу, если файл существует и не просрочен.
     */
    suspend fun get(key: String): ByteArray?

    /**
     * Сохранить байты файла по ключу с опциональным временем жизни (TTL) в миллисекундах.
     */
    suspend fun put(key: String, bytes: ByteArray, ttlMillis: Long? = null)

    /**
     * Проверить существование файла.
     */
    suspend fun exists(key: String): Boolean

    /**
     * Удалить файл по ключу.
     */
    suspend fun delete(key: String): Boolean

    /**
     * Удалить устаревшие файлы, возраст которых превышает [maxAgeMillis].
     */
    suspend fun cleanup(maxAgeMillis: Long)
}

/**
 * Заглушка кэша для тестирования или окружений без диска.
 */
class NoOpFileCache : FileCache {
    override suspend fun get(key: String): ByteArray? = null
    override suspend fun put(key: String, bytes: ByteArray, ttlMillis: Long?) {}
    override suspend fun exists(key: String): Boolean = false
    override suspend fun delete(key: String): Boolean = false
    override suspend fun cleanup(maxAgeMillis: Long) {}
}

/**
 * Простой In-Memory кэш в оперативной памяти.
 */
class InMemoryFileCache : FileCache {
    private data class Entry(val data: ByteArray, val expiresAt: Long?)
    private val memory = mutableMapOf<String, Entry>()

    override suspend fun get(key: String): ByteArray? {
        val entry = memory[key] ?: return null
        if (entry.expiresAt != null && System.currentTimeMillis() > entry.expiresAt) {
            memory.remove(key)
            return null
        }
        return entry.data
    }

    override suspend fun put(key: String, bytes: ByteArray, ttlMillis: Long?) {
        val expiresAt = ttlMillis?.let { System.currentTimeMillis() + it }
        memory[key] = Entry(bytes, expiresAt)
    }

    override suspend fun exists(key: String): Boolean {
        return get(key) != null
    }

    override suspend fun delete(key: String): Boolean {
        return memory.remove(key) != null
    }

    override suspend fun cleanup(maxAgeMillis: Long) {
        val now = System.currentTimeMillis()
        memory.entries.removeIf { (_, entry) ->
            entry.expiresAt != null && now > entry.expiresAt
        }
    }
}
