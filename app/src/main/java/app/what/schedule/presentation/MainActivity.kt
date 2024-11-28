package app.what.schedule.presentation

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.what.schedule.data.remote.impl.rksi.RKSIScheduleApi
import app.what.schedule.presentation.theme.WHATScheduleTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var client: HttpClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        client = HttpClient(OkHttp)

        val api = RKSIScheduleApi(client)

        enableEdgeToEdge()
        setContent {
            WHATScheduleTheme {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val scope = rememberCoroutineScope()

                    Button(
                        onClick = {
                            scope.launch {
//                                api.getTeacherSchedule("Арутюнян А.Э.")
//                                api.getGroupSchedule("ИС-23")
                                val groups = api.getGroups()
                                val teachers = api.getTeachers()

                                Log.d("d", groups.toString())
                                Log.d("d", teachers.toString())

                            }
                        }
                    ) {
                        Text("search")
                    }
                }
            }
        }
    }
}