package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.swipeToSwitchArticle
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalSwipeToSwitchArticle = compositionLocalOf { SwipeToSwitchArticlePreference.default }

class SwipeToSwitchArticlePreference(val value: Boolean) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.swipeToSwitchArticle, value)
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) =
        SwipeToSwitchArticlePreference(!value).put(context, scope)

    companion object {
        val default = SwipeToSwitchArticlePreference(true)
        fun fromPreference(preference: Preferences): SwipeToSwitchArticlePreference {
            return SwipeToSwitchArticlePreference(
                preference[DataStoreKey.keys[swipeToSwitchArticle]?.key as Preferences.Key<Boolean>] ?: return default
            )
        }
    }
}
