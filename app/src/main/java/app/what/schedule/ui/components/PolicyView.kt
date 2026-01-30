package app.what.schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.animations.rememberShimmer
import app.what.foundation.ui.useState
import app.what.schedule.BuildConfig
import app.what.schedule.utils.StringUtils.parseMarkdown
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.koin.compose.koinInject

@Composable
fun PolicyView() = Column {
    val client = koinInject<HttpClient>()
    var content by useState<AnnotatedString?>(null)
    var isError by useState(false)
    val shimmer = rememberShimmer()
    val uriHandler = LocalUriHandler.current
    val colors = colorScheme

    LaunchedEffect(Unit) {
        try {
            val response = client.get(BuildConfig.PRIVACY_POLICY_URL)
            if (response.status.isSuccess()) {
                content = parseMarkdown(response.bodyAsText(), colors)
            } else {
                isError = true
            }
        } catch (e: Exception) {
            isError = true
        }
    }

    if (content != null) SelectionContainer(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(content!!)
    } else if (!isError) repeat(3) {
        Box(
            Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth(if (it < 2) 1f else .6f)
                .height(20.dp)
                .clip(CircleShape)
                .background(shimmer)
        )

        Gap(8)
    } else Fallback(
        "Не удалось загрузить политику конфиденциальности",
        action = "Прочитать браузере" to { uriHandler.openUri(BuildConfig.PRIVACY_POLICY_URL) }
    )
}