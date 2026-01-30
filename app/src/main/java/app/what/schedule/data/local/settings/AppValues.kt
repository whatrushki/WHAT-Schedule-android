package app.what.schedule.data.local.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import app.what.foundation.data.settings.Named
import app.what.foundation.data.settings.PreferenceStorage
import app.what.schedule.data.remote.api.models.ScheduleSearch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
class ScheduleProvider(
    val inst: String,
    val filial: String,
    val provider: String
)

@Serializable
enum class ThemeType(override val displayName: String) : Named {
    Light("Светлая"),
    Dark("Тёмная"),
    System("Системная")
}

@Serializable
enum class ThemeStyle(override val displayName: String) : Named {
    Default("По умолчанию"),
    Material("Material"),
    CustomColor("Свой цвет")
}


@Composable
fun ProvideGLobalAppValues(appValues: AppValues, content: @Composable () -> Unit) =
    CompositionLocalProvider(
        LocalAppValues provides appValues,
        content = content
    )


private val LocalAppValues = staticCompositionLocalOf<AppValues> {
    error("AppValues не предоставлен")
}

@Composable
fun rememberAppValues() = LocalAppValues.current

class AppValues(context: Context) : PreferenceStorage(
    context.getSharedPreferences("MY_APP_PREFERENCES", Context.MODE_PRIVATE)
) {
    val userId = createValue(
        "user_id", null, String.serializer(),
        "Идентификатор пользоваетля", "Уникальный ID установки"
    )

    val isFirstLaunch = createValue(
        "is_first_launch", true, Boolean.serializer(),
        "Первый запуск", "Отслеживание первого запуска приложения"
    )

    val lastSearch = createValue(
        "last_search", null, ScheduleSearch.serializer(),
        "Последний поиск", "Сохраненные параметры последнего поиска расписания"
    )

    val institution = createValue(
        "institution", null, String.serializer(),
        "Учебное заведение", "Выбранное учебное заведение для отображения расписания"
    )

    val themeType = createValue(
        "theme_type", ThemeType.System, ThemeType.serializer(),
        "Тип темы", "Режим темы: светлая, темная или системная"
    )

    val themeStyle = createValue(
        "theme_style", ThemeStyle.Default, ThemeStyle.serializer(),
        "Стиль темы", "Визуальный стиль интерфейса"
    )

    val themeColor = createValue(
        "theme_color", Color(0xFF94FF28).value, ULong.serializer(),
        "Цвет темы", "Основной цвет оформления приложения"
    )

    val useAnimation = createValue(
        "use_animation", true, Boolean.serializer(),
        "Анимации", "Включение анимаций интерфейса"
    )

    val isAnalyticsEnabled = createValue(
        "is_analytics_enabled", true, Boolean.serializer(),
        "Анализ пользования", "Разрешите собирать анонимную статистику пользования"
    )

    val thePolicy = createValue(
        "the_policy", null, String.serializer(),
        "Политика конфиденциальности", "Ознакомлены с политикой и условиями пользования"
    )

    val devSettingsUnlocked = createValue(
        "dev_settings_unlocked", false, Boolean.serializer(),
        "Настройки разработчика", "Доступ к настройкам разработчика"
    )

    val devPanelEnabled = createValue(
        "dev_panel_enabled", false, Boolean.serializer(),
        "Дебаг панель", "Панель для отслеживания действий в приложении"
    )

    val debugMode = createValue(
        "debug_mode", false, Boolean.serializer(),
        "Режим отладки", "Включение дополнительной информации для разработки"
    )
}

