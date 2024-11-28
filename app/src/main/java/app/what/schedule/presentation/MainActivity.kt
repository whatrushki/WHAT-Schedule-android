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
import app.what.schedule.data.remote.impl.turtle.TurtleScheduleApi
import app.what.schedule.presentation.theme.WHATScheduleTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var client: HttpClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val api = TurtleScheduleApi(client)

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
                                val groupSchedule = api.getGroupSchedule("ИС-23")
                                val teachersSchedule = api.getTeacherSchedule("ИС-23")
//                                val groups = api.getGroups()
//                                val teachers = api.getTeachers()

//                                Log.d("d", groups.toString())
//                                Log.d("d", teachers.toString())
                                Log.d("d", groupSchedule.toString())

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
