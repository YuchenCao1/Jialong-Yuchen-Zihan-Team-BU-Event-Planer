package com.example.bueventplaner.ui.pages

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bueventplaner.R
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.services.FirebaseService
import io.github.boguszpawlowski.composecalendar.Calendar
import io.github.boguszpawlowski.composecalendar.day.Day
import io.github.boguszpawlowski.composecalendar.rememberCalendarState
import java.time.*
import java.time.format.DateTimeFormatter


private val eventDateTimeParser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun Event.startLocalDate(): LocalDate {
    val startDateTime = LocalDateTime.parse(this.startTime, eventDateTimeParser)
    return startDateTime.toLocalDate()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(
    navController: NavController,
    addedEvents: List<Event>
) {
    val calendarState = rememberCalendarState(
        initialMonth = YearMonth.now()
    )

    val eventsByDate = remember(addedEvents) {
        addedEvents.groupBy { it.startLocalDate() }
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Events Calendar", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("event_list") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Select a date to view added events",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            // Ensure the Calendar takes enough vertical space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Let the calendar fill the available vertical space
            ) {
                Calendar(
                    calendarState = calendarState,
                    dayContent = { day: Day ->
                        val date = day.date
                        val dayEvents = eventsByDate[date] ?: emptyList()

                        val backgroundColor = when {
                            day.isCurrentDay -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            !day.isFromCurrentMonth -> Color.LightGray.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        }

                        val maxVisibleEvents = 3

                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
                                .fillMaxSize()
                                .clickable {
                                    selectedDate = date
                                },
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp) // Adjust height if needed
                            ) {
                                items(dayEvents.take(maxVisibleEvents)) { event ->
                                    EventChip(event = event)
                                }

                                if (dayEvents.size > maxVisibleEvents) {
                                    item {
                                        Text(
                                            text = "+${dayEvents.size - maxVisibleEvents} more",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            val eventsToday = selectedDate?.let { eventsByDate[it] } ?: emptyList()

            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                if (eventsToday.isEmpty()) {
                    item {
                        Text(
                            text = "No events on this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(eventsToday) { event ->
                        EventItem(event = event)
                    }
                }
            }
        }
    }
}


@Composable
fun EventChip(event: Event) {
    val chipColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .background(chipColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EventItem(event: Event) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()

    // Parse startTime and endTime from the event
    // Adjust format if your timestamps are different
    val startDateTime = LocalDateTime.parse(event.startTime, eventDateTimeParser)
    val endDateTime = LocalDateTime.parse(event.endTime, eventDateTimeParser)

    // Format for display
    val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val startText = startDateTime.format(displayFormatter)
    val endText = endDateTime.format(displayFormatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
        Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "Time: $startText - $endText",
            style = MaterialTheme.typography.bodySmall
        )
        // Optionally display other fields like description, eventUrl, etc.
        // Text(text = event.description)
        // If you have an image from event.photo, consider adding an AsyncImage here (Coil)

        Button(onClick = { context.addEventToGoogleCalendar(event) }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Add to Google Calendar")
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

fun Context.addEventToGoogleCalendar(event: Event) {
    // Parse times again for calendar insertion
    val startDateTime = LocalDateTime.parse(event.startTime, eventDateTimeParser)
    val endDateTime = LocalDateTime.parse(event.endTime, eventDateTimeParser)

    val zoneId = ZoneId.systemDefault()
    val startMillis = startDateTime.atZone(zoneId).toInstant().toEpochMilli()
    val endMillis = endDateTime.atZone(zoneId).toInstant().toEpochMilli()

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.title)
        putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        // Optionally set the description
        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        // If eventUrl is meaningful (e.g., a meeting link), consider adding it to description
    }

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, "No calendar app found.", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun CalendarRoute(navController: NavController) {
    val context = LocalContext.current
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch events once when this composable is first entered
    LaunchedEffect(Unit) {
        FirebaseService.fetchEvents(context) { fetchedEvents ->
            allEvents = fetchedEvents
            isLoading = false
        }
    }

    if (isLoading) {
        // Show a loading indicator while events are being fetched
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Now that events are loaded, pass them to CalendarPage
        CalendarPage(navController = navController, addedEvents = allEvents)
    }
}

