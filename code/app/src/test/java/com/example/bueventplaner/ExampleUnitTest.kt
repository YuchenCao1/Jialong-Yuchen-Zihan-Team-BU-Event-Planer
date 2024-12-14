package com.example.bueventplaner

import com.example.bueventplaner.data.model.Event
import org.junit.Test
import java.time.LocalDate

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EventUtilsTest {

    @Test
    fun `check if events are sorted correctly`() {
        val events = listOf(
            Event(
                id = "1",
                title = "Event 1",
                description = "Test Event 1",
                startTime = "2024-12-10",
                endTime = "2024-12-11"
            ),
            Event(
                id = "2",
                title = "Event 2",
                description = "Test Event 2",
                startTime = "2024-12-09",
                endTime = "2024-12-10"
            )
        )

        val sortedEvents = events.sortedBy { it.startTime }

        assertEquals("2024-12-09", sortedEvents.first().startTime)
        assertEquals("2024-12-10", sortedEvents.last().startTime)
    }

}
