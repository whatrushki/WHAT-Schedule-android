package app.what.schedule.desktop.model

enum class UniType(val title: String) {
    RKSI("РКСИ"),
    DGTU("ДГТУ"),
    IUBIP("ИУБиП"),
    RINH("РИНХ")
}

data class SearchItem(
    val id: String,
    val title: String,
    val isTeacher: Boolean
)
