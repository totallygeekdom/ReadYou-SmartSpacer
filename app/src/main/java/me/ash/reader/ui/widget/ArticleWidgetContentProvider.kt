package me.ash.reader.ui.widget

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ArticleWidgetContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "me.ash.reader.widget.articles"
        val COLUMNS = arrayOf("id", "feed_name", "title", "date", "feed_id", "filter_feed_id", "filter_group_id", "is_read")
        private const val TAG = "ArticleWidgetProvider"

        private const val MATCH_ARTICLES = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "#", MATCH_ARTICLES)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        if (uriMatcher.match(uri) != MATCH_ARTICLES) return null
        val widgetId = uri.lastPathSegment?.toIntOrNull() ?: return null

        val repo = WidgetRepository.get(context!!)
        val config = runBlocking { repo.getConfig(widgetId) }
        val articles = runBlocking { repo.getData(config.dataSource).first() }.articles

        val filterFeedId = (config.dataSource as? DataSource.Feed)?.feedId
        val filterGroupId = (config.dataSource as? DataSource.Group)?.groupId

        Log.d(TAG, "widgetId=$widgetId articleCount=${articles.size} readCount=${articles.count { it.isRead }}")
        articles.take(15).forEachIndexed { i, a ->
            Log.d(TAG, "  [$i] isRead=${a.isRead} id=${a.id} title=\"${a.title}\"")
        }

        val cursor = MatrixCursor(COLUMNS)
        articles.take(15).forEach { article ->
            cursor.addRow(arrayOf(article.id, article.feedName, article.title, article.date, article.feedId, filterFeedId, filterGroupId, if (article.isRead) 1 else 0))
        }
        return cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
