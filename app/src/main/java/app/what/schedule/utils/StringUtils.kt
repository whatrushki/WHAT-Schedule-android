package app.what.schedule.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object StringUtils {
    fun parseMarkdown(text: String, colors: ColorScheme): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEach { line ->
                when {

                    line.startsWith("# ") -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colors.primary
                            )
                        ) {
                            append(line.removePrefix("# ") + "\n")
                        }
                    }

                    line.startsWith("## ") -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.secondary
                            )
                        ) {
                            append(line.removePrefix("## ") + "\n")
                        }
                    }

                    line.startsWith("### ") -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colors.secondary
                            )
                        ) {
                            append(line.removePrefix("### ") + "\n")
                        }
                    }

                    line.contains("**") -> {
                        val parts = line.split("**")
                        parts.forEachIndexed { index, part ->
                            if (index % 2 == 1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(part)
                                }
                            } else {
                                append(part)
                            }
                        }
                        append("\n")
                    }

                    else -> {
                        append(line + "\n")
                    }
                }
            }
        }
    }
}