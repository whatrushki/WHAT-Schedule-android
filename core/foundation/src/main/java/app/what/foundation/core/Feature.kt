package app.what.foundation.core

typealias Listener<T> = (T) -> Unit

abstract class Feature<Ctrl : UIController<*, *, Event>, Event : Any> : UIComponent {
    protected abstract val controller: Ctrl
    protected val listener: Listener<Event> by lazy { controller::obtainEvent }
}