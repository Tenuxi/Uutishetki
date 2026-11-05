package fi.uutishetki

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request

class NewsApplication : Application(), ImageLoaderFactory {
    val client by lazy {
        OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS).retryOnConnectionFailure(false)
            .cache(Cache(File(cacheDir, "http"), 8L * 1024 * 1024)).build()
    }
    val repository by lazy { NewsRepository(this, client) }
    override fun newImageLoader() = ImageLoader.Builder(this)
        .okHttpClient(client)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.15).build() }
        .diskCache { DiskCache.Builder().directory(File(cacheDir, "images")).maxSizeBytes(80L * 1024 * 1024).build() }
        .crossfade(true).build()
}

class NewsRepository(app: Application, private val client: OkHttpClient) {
    private val store = NewsStore(app)
    private val prefs = app.getSharedPreferences("refresh", 0)
    private val refreshMutex = Mutex()
    private val storageMutex = Mutex()
    val news = MutableStateFlow<List<News>>(emptyList())
    val loading = MutableStateFlow(true)
    val refreshing = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)
    val lastUpdated = MutableStateFlow(prefs.getLong("lastSuccess", 0))
    private var loaded = false
    private var lastAttempt = 0L

    suspend fun refresh(force: Boolean = false) {
        if (!refreshMutex.tryLock()) return
        try {
            withContext(Dispatchers.IO) {
                if (!loaded) {
                    try { news.value = store.read(); loaded = true }
                    catch (e: Exception) { message.value = "Tallennettuja uutisia ei voitu lukea." }
                    finally { loading.value = false }
                }
                val now = System.currentTimeMillis()
                if (!force && now - maxOf(lastAttempt, lastUpdated.value) in 0 until 5 * 60_000L) return@withContext
                if (force && now - lastAttempt in 0 until 10_000L) return@withContext
                lastAttempt = now
                refreshing.value = true
                message.value = null
                val failures = supervisorScope {
                    Source.entries.map { source -> async {
                        try {
                            val request = Request.Builder().url(source.feed)
                                .header("User-Agent", "Uutishetki/${BuildConfig.VERSION_NAME} (personal RSS reader)")
                                .header("Cache-Control", "max-age=300").build()
                            val items = client.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                                val body = response.body ?: throw IOException("Tyhjä vastaus")
                                val bytes = body.byteStream().use { it.readBytesLimited(2 * 1024 * 1024) }
                                FeedParser.parse(bytes.toString(Charsets.UTF_8), source)
                            }
                            storageMutex.withLock {
                                store.merge(items, now)
                                news.value = store.read()
                            }
                            null
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) { source.label }
                    } }.awaitAll().filterNotNull()
                }
                if (failures.size < Source.entries.size) {
                    lastUpdated.value = now
                    prefs.edit().putLong("lastSuccess", now).apply()
                }
                if (failures.isNotEmpty()) message.value =
                    "Päivitys ei onnistunut: ${failures.joinToString(", ")}. Aiemmin ladatut uutiset säilyvät."
            }
        } finally {
            refreshing.value = false
            loading.value = false
            refreshMutex.unlock()
        }
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        if (output.size() + count > limit) throw IOException("Liian suuri syöte")
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}
