package app.what.schedule.features.settings.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import app.what.foundation.ui.useSave
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data class GithubRepoInfo(
    @SerialName("stargazers_count") val stars: Int
)

@Composable
fun rememberGithubStars(
    owner: String,
    repo: String
): String? {
    var stars by useSave<String?>(null)
    val client = koinInject<HttpClient>()

    LaunchedEffect(Unit) {
        if (stars == null) try {
            val response = client.get("https://api.github.com/repos/$owner/$repo")
            if (response.status.isSuccess()) {
                val info = response.body<GithubRepoInfo>()
                stars = info.stars.toString()
            }
        } catch (e: Exception) {

        }
    }

    return stars
}