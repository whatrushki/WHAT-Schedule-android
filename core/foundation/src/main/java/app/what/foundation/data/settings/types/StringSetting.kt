package app.what.foundation.data.settings.types

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import app.what.foundation.data.settings.Setting
import app.what.foundation.data.settings.supporting.StringEditWithValidation
import app.what.foundation.data.settings.views.BaseSettingView
import app.what.foundation.ui.controllers.rememberDialogController
import app.what.foundation.ui.useState

data class ValidationRules(
    val required: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: Regex? = null,
    val patternError: String? = null
) {
    companion object {
        val Empty = ValidationRules()
    }
}

val ValidatedStringSetting = @Composable
fun(
    modifier: Modifier,
    setting: Setting<String>,
    validationRules: ValidationRules,
) {

    fun validate(value: String): String? {
        return when {
            validationRules.required && value.isBlank() -> "Поле обязательно для заполнения"
            validationRules.minLength != null && value.length < validationRules.minLength ->
                "Минимум ${validationRules.minLength} символов"

            validationRules.maxLength != null && value.length > validationRules.maxLength ->
                "Максимум ${validationRules.maxLength} символов"

            validationRules.pattern != null && !validationRules.pattern.matches(value) ->
                validationRules.patternError ?: "Неверный формат"

            else -> null
        }
    }

    val currentValue by setting.value.collect()
    val dialogController = rememberDialogController()
    var tempValue by useState(currentValue ?: "")
    var error by useState<String?>(null)

    BaseSettingView(
        modifier = modifier,
        icon = setting.icon,
        title = setting.title,
        description = setting.description,
        trailing = {
            Text(
                text = currentValue ?: "Не задано",
                style = typography.bodyMedium,
                color = if (currentValue.isNullOrEmpty()) colorScheme.error
                else colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            StringEditWithValidation(
                title = "Редактировать",
                currentValue = tempValue,
                hint = "Введите значение",
                error = error,
                onValueChange = {
                    tempValue = it
                    error = validate(it)
                },
                onSave = {
                    if (error == null) {
                        setting.value.set(tempValue)
                        dialogController.close()
                    }
                },
                onDismiss = {
                    tempValue = currentValue ?: ""
                    error = null
                    dialogController.close()
                }
            )
        }
    )
}