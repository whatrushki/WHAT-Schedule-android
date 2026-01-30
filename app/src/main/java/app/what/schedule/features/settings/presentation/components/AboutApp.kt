package app.what.schedule.features.settings.presentation.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.animations.AdvancedLiquidBackground
import app.what.foundation.ui.animations.AnimatedEnter
import app.what.foundation.ui.animations.wiggle
import app.what.foundation.ui.bclick
import app.what.foundation.ui.useState
import app.what.schedule.BuildConfig
import app.what.schedule.data.local.settings.AppValues
import app.what.schedule.features.settings.presentation.utils.rememberGithubStars
import app.what.schedule.ui.components.AsyncImageWithFallback
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.ImageRoller
import app.what.schedule.ui.theme.icons.filled.Telegram
import app.what.schedule.utils.Analytics
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AboutAppContent(
    appValues: AppValues,
    modifier: Modifier = Modifier
) {
    val devSettingsUnlocked by appValues.devSettingsUnlocked.collect()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    var versionClickCount by useState(0)
    var showFireworks by useState(false)

    LaunchedEffect(versionClickCount) {
        if (versionClickCount == 10) {
            showFireworks = true
            Toast.makeText(
                context,
                "🎉 Developer Mode Unlocked!",
                Toast.LENGTH_LONG
            ).show()

            Analytics.logEasterEggFound("version taps in 'about app'")
            appValues.devSettingsUnlocked.set(true)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .clip(shapes.medium)
    ) {
        val layer1 = colorScheme.primaryContainer to colorScheme.tertiaryContainer
        val layer2 = colorScheme.secondaryContainer to colorScheme.surfaceContainer

        AdvancedLiquidBackground(
            layers = listOf(layer1, layer2)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier.padding(12.dp, 24.dp, 12.dp, 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DevProfile(
                avatarUrl = BuildConfig.APP_OWNER_GITHUB_AVATAR_URL,
                name = BuildConfig.APP_OWNER_GITHUB_NICKNAME,
                role = BuildConfig.APP_OWNER_ROLE
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialChip(
                    icon = WHATIcons.ImageRoller,
                    text = "GitHub",
                    onClick = { uriHandler.openUri(BuildConfig.APP_OWNER_GITHUB_URL) }
                )
                SocialChip(
                    icon = WHATIcons.Telegram,
                    text = "Telegram",
                    color = Color(0xFF2AABEE),
                    onClick = { uriHandler.openUri(BuildConfig.APP_OWNER_TELEGRAM_URL) }
                )
            }

            RepoStatsCard(
                owner = BuildConfig.APP_GITHUB_URL.split("/").dropLast(1).last(),
                repo = BuildConfig.APP_GITHUB_URL.split("/").last(),
                repoUrl = BuildConfig.APP_GITHUB_URL,
                uriHandler = uriHandler
            )

            JoinTeamCard(
                onClick = { uriHandler.openUri(BuildConfig.APP_OWNER_TELEGRAM_URL) }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WHAT Schedule v${BuildConfig.VERSION_NAME}",
                    style = typography.labelMedium,
                    color = colorScheme.secondary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .bclick(devSettingsUnlocked == false) { versionClickCount++ }
                        .padding(8.dp)
                )

                AnimatedVisibility(versionClickCount in 3..9) {
                    Text(
                        text = "${10 - versionClickCount}...",
                        style = typography.labelSmall,
                        color = colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (showFireworks) {
            SimpleFireworks(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DevProfile(avatarUrl: String, name: String, role: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .border(
                    2.dp, Brush.linearGradient(
                        listOf(colorScheme.primary, colorScheme.tertiary)
                    ), CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
        ) {
            AsyncImageWithFallback(
                avatarUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        Gap(12)

        Text(
            text = name,
            style = typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Text(
            text = role,
            style = typography.bodyMedium,
            color = colorScheme.secondary
        )
    }
}

@Composable
private fun SocialChip(
    icon: ImageVector,
    text: String,
    color: Color = colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = shapes.medium,
        color = colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Gap(8)
            Text(
                text,
                style = typography.labelLarge,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RepoStatsCard(
    owner: String,
    repo: String,
    repoUrl: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val stars = rememberGithubStars(owner, repo)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.medium)
            .background(
                Brush.horizontalGradient(
                    listOf(colorScheme.primary.copy(0.1f), colorScheme.tertiary.copy(0.1f))
                )
            )
            .bclick { uriHandler.openUri(repoUrl) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Source Code", style = typography.labelSmall, color = colorScheme.secondary)
            Text(
                "GitHub Repository",
                style = typography.titleSmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }


        AnimatedEnter(stars != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icons.Default.Star.Show(
                    Modifier
                        .size(16.dp)
                        .wiggle(15f),
                    Color(0xFFFFB300)
                )
                Gap(4)
                Text(stars ?: "~", style = typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun JoinTeamCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.medium)
            .background(colorScheme.primary)
            .bclick(block = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Хотите помочь проекту?",
                style = typography.titleSmall,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Напишите мне в Telegram",
                style = typography.bodySmall,
                color = colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = colorScheme.onPrimary
        )
    }
}

@Composable
private fun SimpleFireworks(modifier: Modifier = Modifier) {
    val particles = remember { List(50) { FireworkParticle() } }
    val transition = rememberInfiniteTransition(label = "Fireworks")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "Time"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        particles.forEachIndexed { index, particle ->
            val progress = (time + index * 0.02f) % 1f
            val radius = progress * size.width * 0.6f
            val alpha = 1f - progress // Исчезает к концу

            val x = centerX + cos(particle.angle) * radius
            val y = centerY + sin(particle.angle) * radius

            drawCircle(
                color = particle.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = particle.size * (1f - progress * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

private data class FireworkParticle(
    val angle: Float = (Math.random() * 2 * Math.PI).toFloat(),
    val color: Color = listOf(
        Color(0xFFFF5252), Color(0xFF448AFF), Color(0xFF69F0AE), Color(0xFFFFD740)
    ).random(),
    val size: Float = (Math.random() * 10 + 5).toFloat()
)