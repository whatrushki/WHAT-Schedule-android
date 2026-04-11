package app.what.schedule.features.insts.dgtu.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.what.foundation.core.Listener
import app.what.foundation.ui.Gap
import app.what.foundation.ui.Show
import app.what.foundation.ui.useState
import app.what.schedule.features.insts.dgtu.domain.models.DgtuEvent
import app.what.schedule.ui.components.StyledTextField
import app.what.schedule.ui.theme.icons.WHATIcons
import app.what.schedule.ui.theme.icons.filled.Building
import app.what.schedule.ui.theme.icons.filled.Features

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    DGTULoginScreen {}
}


@Composable
internal fun DGTULoginScreen(
    listener: Listener<DgtuEvent>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(shape = shapes.large)
                .background(colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = WHATIcons.Building,
                contentDescription = "App icon",
                tint = colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }
        
        Gap(40)
        
        Text(
            text = "Вход в кабинет студента",
            style = typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        
        Gap(8)
        
        Text(
            text = "Введи данные студента, чтобы увидеть своё расписание",
            style = typography.bodyLarge,
            color = colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        
        Gap(16)
        
        val (login, setLogin) = useState("")
        val (password, setPass) = useState("")
        
        StyledTextField(
            login, setLogin,
            modifier = Modifier.fillMaxWidth(),
            debounce = 500,
            placeholder = "Логин",
            options = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = shapes.medium,
            leading = {
                WHATIcons.Features.Show(colorScheme.secondary, 22)
            }
        )
        
        Gap(12)
        
        StyledTextField(
            password, setPass,
            modifier = Modifier.fillMaxWidth(),
            debounce = 500,
            placeholder = "Пароль",
            options = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = shapes.medium,
            leading = {
                WHATIcons.Features.Show(colorScheme.secondary, 22)
            }
        )
        
        Gap(16)
        
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.medium,
            onClick = {
                listener(DgtuEvent.AuthClicked(login, password))
            }
        ) {
            Text(
                "Войти",
                modifier = Modifier
            )
        }
    }
}



