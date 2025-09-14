package app.what.schedule.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.what.foundation.ui.useState
import kotlinx.coroutines.delay

@Composable
fun SearchBox(
    query: String,
    setQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (value, setValue) = useState(query)

    LaunchedEffect(value) {
        delay(500)
        setQuery(value)
    }

    TextField(
        value,
        setValue,
        modifier = Modifier
            .focusable(true)
            .fillMaxWidth()
            .then(modifier),
        singleLine = true,
        placeholder = { Text("Поиск...") },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = "search"
            )
        }
    )
}