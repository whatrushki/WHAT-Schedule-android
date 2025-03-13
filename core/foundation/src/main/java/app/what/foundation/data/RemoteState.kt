package app.what.foundation.data

sealed interface RemoteState {
    object Idle : RemoteState
    object Loading : RemoteState
    object Error : RemoteState
    object Success : RemoteState
    object Empty : RemoteState
}