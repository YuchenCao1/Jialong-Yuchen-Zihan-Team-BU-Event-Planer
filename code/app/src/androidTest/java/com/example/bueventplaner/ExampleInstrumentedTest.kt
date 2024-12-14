package com.example.bueventplaner

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bueventplaner.data.repository.EventDao
import com.example.bueventplaner.ui.pages.EventDetailsView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.example.bueventplaner.data.model.EventEntity
import kotlinx.coroutines.flow.MutableStateFlow

@RunWith(AndroidJUnit4::class)
class EventUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEventDetailsView() {
        composeTestRule.setContent {
            EventDetailsView(
                eventDao = FakeEventDao(),
                eventId = "testEventId",
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("eventDetailsView").assertIsDisplayed()
    }
}

class FakeEventDao : EventDao {

    private val eventList = MutableStateFlow<List<EventEntity>>(emptyList())

    override fun insertEvents(events: List<EventEntity>) {
        eventList.value = eventList.value + events
    }

    override fun getAllEvents(): Flow<List<EventEntity>> {
        return eventList
    }

    override fun getEventById(eventId: String): Flow<EventEntity?> {
        val event = eventList.value.find { it.id == eventId }
        return flowOf(event)
    }

    override fun deleteAllEvents() {
        eventList.value = emptyList()
    }
}
