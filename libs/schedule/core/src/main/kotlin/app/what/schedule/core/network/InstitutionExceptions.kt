package app.what.schedule.core.network

sealed class InstitutionException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String, cause: Throwable? = null) : InstitutionException(message, cause)
    class Parsing(message: String, cause: Throwable? = null) : InstitutionException(message, cause)
    class Throttled(message: String) : InstitutionException(message)
    class Unauthorized(message: String) : InstitutionException(message)
    class ServerError(val code: Int, message: String) : InstitutionException("HTTP $code: $message")
}
