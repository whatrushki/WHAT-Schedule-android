package app.what.foundation.data

sealed interface RemoteState {
    object Idle : RemoteState
    object Loading : RemoteState
    class Error(e: Exception) : RemoteState
    object Success : RemoteState
    object Empty : RemoteState
}