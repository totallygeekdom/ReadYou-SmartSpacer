package me.ash.reader.ui.widget

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ArticleWidgetContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "me.ash.reader.widget.articles"
        val COLUMNS = arrayOf("id", "feed_name", "title", "date")

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

        val cursor = MatrixCursor(COLUMNS)
        articles.take(15).forEach { article ->
            cursor.addRow(arrayOf(article.id, article.feedName, article.title, article.date))
        }
        return cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
