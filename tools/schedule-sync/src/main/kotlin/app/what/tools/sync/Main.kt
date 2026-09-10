package app.what.tools.sync

import app.what.schedule.core.models.DayScheduleDto
import app.what.schedule.core.models.GroupDto
import app.what.schedule.core.models.TeacherDto
import app.what.schedule.dgtu.DGTUScheduleClient
import app.what.schedule.iubip.IUBIPScheduleClient
import app.what.schedule.rinh.RINHScheduleClient
import app.what.schedule.rksi.RKSIScheduleClient
import app.what.schedule.rksi.parser.JvmXlsxReader
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class InstitutionMeta(
    val lastSync: String,
    val institution: String,
    val groupCount: Int,
    val teacherCount: Int
)

@Serializable
data class InstitutionData(
    val lastSync: String,
    val institution: String,
    val groups: List<GroupDto>,
    val teachers: List<TeacherDto>,
    val schedules: Map<String, List<DayScheduleDto>> = emptyMap()
)

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    val targetIndex = args.indexOf("--target")
    val target = if (targetIndex in 0 until args.size - 1) args[targetIndex + 1] else "all"
    val outIndex = args.indexOf("--out")
    val outPath = if (outIndex in 0 until args.size - 1) args[outIndex + 1] else ".github/schedule"
    val outDir = File(outPath)
    outDir.mkdirs()

    println("=== Starting Schedule Sync ===")
    println("Target: $target")
    println("Output: ${outDir.absolutePath}")

    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    try {
        when (target.lowercase()) {
            "rksi" -> syncRksi(client, outDir)
            "others" -> syncOthers(client, outDir)
            "all" -> {
                syncRksi(client, outDir)
                syncOthers(client, outDir)
            }
            else -> {
                println("Unknown target: $target. Use 'rksi', 'others', or 'all'.")
            }
        }
    } finally {
        client.close()
        println("=== Schedule Sync Finished ===")
    }
}

suspend fun syncRksi(client: HttpClient, rootDir: File) {
    println("\n--- Syncing RKSI ---")
    val dir = File(rootDir, "rksi").apply { mkdirs() }
    val groupsDir = File(dir, "groups").apply { mkdirs() }

    runCatching {
        val rksiClient = RKSIScheduleClient(
            client = client,
            xlsxReader = JvmXlsxReader(),
            log = { println("  [RKSI] $it") }
        )

        val nowStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        println("  Fetching RKSI groups & teachers...")
        val groups = rksiClient.getGroups()
        val teachers = rksiClient.getTeachers()
        println("  Found ${groups.size} groups and ${teachers.size} teachers")

        File(dir, "groups.json").writeText(json.encodeToString(groups))
        File(dir, "teachers.json").writeText(json.encodeToString(teachers))

        val schedules = mutableMapOf<String, List<DayScheduleDto>>()
        val semaphore = Semaphore(5)

        coroutineScope {
            groups.map { group ->
                async {
                    semaphore.withPermit {
                        runCatching {
                            val schedule = rksiClient.getGroupSchedule(group.name)
                            if (schedule.isNotEmpty()) {
                                synchronized(schedules) {
                                    schedules[group.name] = schedule
                                }
                                val safeName = group.name.replace("/", "_").replace("\\", "_")
                                File(groupsDir, "$safeName.json").writeText(json.encodeToString(schedule))
                            }
                        }.onFailure {
                            println("  [RKSI] Failed schedule for ${group.name}: ${it.message}")
                        }
                    }
                }
            }.awaitAll()
        }

        val meta = InstitutionMeta(
            lastSync = nowStr,
            institution = "rksi",
            groupCount = groups.size,
            teacherCount = teachers.size
        )
        File(dir, "meta.json").writeText(json.encodeToString(meta))

        val data = InstitutionData(
            lastSync = nowStr,
            institution = "rksi",
            groups = groups,
            teachers = teachers,
            schedules = schedules
        )
        File(dir, "data.json").writeText(json.encodeToString(data))
        println("  [RKSI] Sync complete! Cached ${schedules.size} group schedules.")
    }.onFailure {
        println("  [RKSI] Error syncing: ${it.message}")
        it.printStackTrace()
    }
}

suspend fun syncOthers(client: HttpClient, rootDir: File) {
    syncDgtu(client, rootDir)
    syncIubip(client, rootDir)
    syncRinh(client, rootDir)
}

suspend fun syncDgtu(client: HttpClient, rootDir: File) {
    println("\n--- Syncing DGTU ---")
    val dir = File(rootDir, "dgtu").apply { mkdirs() }
    val groupsDir = File(dir, "groups").apply { mkdirs() }

    runCatching {
        val dgtuClient = DGTUScheduleClient(client, log = { println("  [DGTU] $it") })
        val nowStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        println("  Fetching DGTU groups & teachers...")
        val groups = dgtuClient.getGroups()
        val teachers = dgtuClient.getTeachers()
        println("  Found ${groups.size} groups and ${teachers.size} teachers")

        File(dir, "groups.json").writeText(json.encodeToString(groups))
        File(dir, "teachers.json").writeText(json.encodeToString(teachers))

        val meta = InstitutionMeta(
            lastSync = nowStr,
            institution = "dgtu",
            groupCount = groups.size,
            teacherCount = teachers.size
        )
        File(dir, "meta.json").writeText(json.encodeToString(meta))
        println("  [DGTU] Sync complete!")
    }.onFailure {
        println("  [DGTU] Error syncing: ${it.message}")
        it.printStackTrace()
    }
}

suspend fun syncIubip(client: HttpClient, rootDir: File) {
    println("\n--- Syncing IUBIP ---")
    val dir = File(rootDir, "iubip").apply { mkdirs() }
    val groupsDir = File(dir, "groups").apply { mkdirs() }

    runCatching {
        val iubipClient = IUBIPScheduleClient(client, log = { println("  [IUBIP] $it") })
        val nowStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        println("  Fetching IUBIP groups & teachers...")
        val groups = iubipClient.getGroups()
        val teachers = iubipClient.getTeachers()
        println("  Found ${groups.size} groups and ${teachers.size} teachers")

        File(dir, "groups.json").writeText(json.encodeToString(groups))
        File(dir, "teachers.json").writeText(json.encodeToString(teachers))

        val schedules = mutableMapOf<String, List<DayScheduleDto>>()
        val semaphore = Semaphore(4)

        coroutineScope {
            groups.map { group ->
                async {
                    semaphore.withPermit {
                        runCatching {
                            delay(50)
                            val schedule = iubipClient.getGroupSchedule(group.name)
                            if (schedule.isNotEmpty()) {
                                synchronized(schedules) {
                                    schedules[group.name] = schedule
                                }
                                val safeName = group.name.replace("/", "_").replace("\\", "_")
                                File(groupsDir, "$safeName.json").writeText(json.encodeToString(schedule))
                            }
                        }
                    }
                }
            }.awaitAll()
        }

        val meta = InstitutionMeta(
            lastSync = nowStr,
            institution = "iubip",
            groupCount = groups.size,
            teacherCount = teachers.size
        )
        File(dir, "meta.json").writeText(json.encodeToString(meta))

        val data = InstitutionData(
            lastSync = nowStr,
            institution = "iubip",
            groups = groups,
            teachers = teachers,
            schedules = schedules
        )
        File(dir, "data.json").writeText(json.encodeToString(data))
        println("  [IUBIP] Sync complete! Cached ${schedules.size} group schedules.")
    }.onFailure {
        println("  [IUBIP] Error syncing: ${it.message}")
        it.printStackTrace()
    }
}

suspend fun syncRinh(client: HttpClient, rootDir: File) {
    println("\n--- Syncing RINH ---")
    val dir = File(rootDir, "rinh").apply { mkdirs() }
    val groupsDir = File(dir, "groups").apply { mkdirs() }

    runCatching {
        val rinhClient = RINHScheduleClient(client, log = { println("  [RINH] $it") })
        val nowStr = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

        println("  Fetching RINH groups & teachers...")
        val groups = rinhClient.getGroups()
        val teachers = rinhClient.getTeachers()
        println("  Found ${groups.size} groups and ${teachers.size} teachers")

        File(dir, "groups.json").writeText(json.encodeToString(groups))
        File(dir, "teachers.json").writeText(json.encodeToString(teachers))

        val schedules = mutableMapOf<String, List<DayScheduleDto>>()
        val semaphore = Semaphore(4)

        coroutineScope {
            groups.map { group ->
                async {
                    semaphore.withPermit {
                        runCatching {
                            delay(50)
                            val schedule = rinhClient.getGroupSchedule(group.name)
                            if (schedule.isNotEmpty()) {
                                synchronized(schedules) {
                                    schedules[group.name] = schedule
                                }
                                val safeName = group.name.replace("/", "_").replace("\\", "_")
                                File(groupsDir, "$safeName.json").writeText(json.encodeToString(schedule))
                            }
                        }
                    }
                }
            }.awaitAll()
        }

        val meta = InstitutionMeta(
            lastSync = nowStr,
            institution = "rinh",
            groupCount = groups.size,
            teacherCount = teachers.size
        )
        File(dir, "meta.json").writeText(json.encodeToString(meta))

        val data = InstitutionData(
            lastSync = nowStr,
            institution = "rinh",
            groups = groups,
            teachers = teachers,
            schedules = schedules
        )
        File(dir, "data.json").writeText(json.encodeToString(data))
        println("  [RINH] Sync complete! Cached ${schedules.size} group schedules.")
    }.onFailure {
        println("  [RINH] Error syncing: ${it.message}")
        it.printStackTrace()
    }
}
