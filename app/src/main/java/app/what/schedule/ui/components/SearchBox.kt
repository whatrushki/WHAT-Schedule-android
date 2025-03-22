package app.what.schedule.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SearchBox(
    query: String,
    setQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) = TextField(
    query,
    setQuery,
    modifier = Modifier
        .focusable(true)
        .fillMaxWidth()
        .then(modifier),
    singleLine = true,
    placeholder = { Text("Поиск...") },
    leadingIcon = {
        Icon(
            Icons.Default.Search,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = "search"
        )
    }
)