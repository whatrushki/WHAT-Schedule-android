package app.what.foundation.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.what.foundation.ui.useState

class PreferenceStore(context: Context, name: String) {
    val prefs =  context.getSharedPreferences(name, Context.MODE_PRIVATE)


}