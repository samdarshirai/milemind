package com.company.runcoach.core.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AdaptationEvent(
    val summary: String,
    val affectedFromDate: String,
    val affectedToDate: String,
    val changedWorkoutIds: List<String>
)

object AdaptationEvents {
    private val _events = MutableSharedFlow<AdaptationEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AdaptationEvent> = _events.asSharedFlow()

    suspend fun publish(event: AdaptationEvent) {
        _events.emit(event)
    }
}
