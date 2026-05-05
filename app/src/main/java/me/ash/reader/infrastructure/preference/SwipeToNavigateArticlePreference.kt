package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.swipeToNavigateArticle
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalSwipeToNavigateArticle =
    compositionLocalOf<SwipeToNavigateArticlePreference> { SwipeToNavigateArticlePreference.default }

sealed class SwipeToNavigateArticlePreference(val value: Int) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(swipeToNavigateArticle, value)
        }
    }

    @Composable
    abstract fun description(): String

    object None : SwipeToNavigateArticlePreference(0) {
        @Composable
        override fun description(): String = stringResource(R.string.none)
    }

    object Horizontal : SwipeToNavigateArticlePreference(1) {
        @Composable
        override fun description(): String = stringResource(R.string.horizontal)
    }

    object Vertical : SwipeToNavigateArticlePreference(2) {
        @Composable
        override fun description(): String = stringResource(R.string.vertical)
    }

    companion object {
        val default: SwipeToNavigateArticlePreference = Vertical
        val values = listOf(None, Horizontal, Vertical)

        fun fromPreference(preference: Preferences): SwipeToNavigateArticlePreference {
            val storedValue =
                preference[DataStoreKey.keys[swipeToNavigateArticle]?.key as? Preferences.Key<Int>]
            if (storedValue != null) return fromValue(storedValue)

            // Migrate from old boolean keys
            val hadPull =
                preference[DataStoreKey.keys[DataStoreKey.pullToSwitchArticle]?.key as? Preferences.Key<Boolean>]
            val hadSwipe =
                preference[DataStoreKey.keys[DataStoreKey.swipeToSwitchArticle]?.key as? Preferences.Key<Boolean>]
            return when {
                hadSwipe == true -> Horizontal
                hadPull == true -> Vertical
                hadPull == false && hadSwipe == false -> None
                else -> default
            }
        }

        fun fromValue(value: Int): SwipeToNavigateArticlePreference =
            when (value) {
                0 -> None
                1 -> Horizontal
                2 -> Vertical
                else -> default
            }
    }
}
