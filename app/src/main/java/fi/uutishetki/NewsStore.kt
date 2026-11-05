package fi.uutishetki

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NewsStore(context: Context) : SQLiteOpenHelper(context, "news.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE news (id TEXT PRIMARY KEY, source TEXT NOT NULL, title TEXT NOT NULL,
            url TEXT NOT NULL, teaser TEXT, image TEXT, published INTEGER, topics TEXT NOT NULL, fetched INTEGER NOT NULL)""")
        db.execSQL("CREATE INDEX news_date ON news(published DESC)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun read(): List<News> = readableDatabase.rawQuery("SELECT * FROM news ORDER BY published DESC, id", null).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                fun text(name: String) = cursor.getString(cursor.getColumnIndexOrThrow(name))
                val dateIndex = cursor.getColumnIndexOrThrow("published")
                add(News(text("id"), Source.valueOf(text("source")), text("title"), text("url"),
                    text("teaser"), text("image"), if (cursor.isNull(dateIndex)) null else cursor.getLong(dateIndex),
                    text("topics").split(',').mapNotNull { name -> Topic.entries.find { it.name == name } }.toSet()))
            }
        }
    }
    fun merge(items: List<News>, now: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            mergeNews(items).forEach { incoming ->
                val previousTopics = db.rawQuery("SELECT topics FROM news WHERE id = ?", arrayOf(incoming.id)).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0).split(',').mapNotNull { name -> Topic.entries.find { it.name == name } }.toSet()
                    else emptySet()
                }
                val news = incoming.copy(topics = incoming.topics + previousTopics)
                val values = ContentValues().apply {
                    put("id", news.id); put("source", news.source.name); put("title", news.title)
                    put("url", news.url); put("teaser", news.teaser); put("image", news.image)
                    put("published", news.published); put("topics", news.topics.joinToString(",") { it.name }); put("fetched", now)
                }
                db.insertWithOnConflict("news", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.execSQL("DELETE FROM news WHERE fetched < ?", arrayOf(now - 14L * 24 * 60 * 60 * 1000))
            Source.entries.forEach { source ->
                db.execSQL("DELETE FROM news WHERE source = ? AND id NOT IN (SELECT id FROM news WHERE source = ? ORDER BY published DESC, id LIMIT 300)", arrayOf(source.name, source.name))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
}
