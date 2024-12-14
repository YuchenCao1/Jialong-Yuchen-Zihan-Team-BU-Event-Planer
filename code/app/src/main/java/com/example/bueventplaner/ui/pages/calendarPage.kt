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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bueventplaner.R
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.services.FirebaseService
import com.example.bueventplaner.ui.component.EventCard
import java.time.*
import java.time.format.DateTimeFormatter
import com.google.accompanist.pager.HorizontalPager
import com.google.firebase.auth.FirebaseAuth
import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



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
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val eventsByDate = remember(addedEvents) {
        addedEvents.groupBy { it.startLocalDate() }
    }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("event_list") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
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
            if (isPortrait) {
                // Portrait Layout
                WeeklyCalendarView(
                    currentMonth = currentMonth,
                    eventsByDate = eventsByDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    EventListForSelectedDate(
                        navController = navController,
                        selectedDate = selectedDate,
                        eventsByDate = eventsByDate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Landscape Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        WeeklyCalendarView(
                            currentMonth = currentMonth,
                            eventsByDate = eventsByDate,
                            onDateSelected = { selectedDate = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        EventListForSelectedDate(
                            navController = navController,
                            selectedDate = selectedDate,
                            eventsByDate = eventsByDate,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarView(
    currentMonth: YearMonth,
    eventsByDate: Map<LocalDate, List<Event>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weeksInMonth = getWeeksInMonth(currentMonth)

    HorizontalPager(
        count = weeksInMonth.size,
        modifier = modifier
            .fillMaxWidth()
    ) { pageIndex ->
        val weekDates = weeksInMonth[pageIndex]

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDates.forEach { date ->
                val dayEvents = eventsByDate[date] ?: emptyList()

                val backgroundColor = when {
                    date == LocalDate.now() -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }

                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
                        .clickable { onDateSelected(date) }
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(3), // Day name (e.g., Mon)
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = date.dayOfMonth.toString(), // Day number
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .weight(1f),

                    ) {
                        items(dayEvents.take(3)) { event ->
                            EventChip(event = event)
                        }

                        if (dayEvents.size > 3) {
                            item {
                                Text(
                                    text = "+${dayEvents.size - 3} more",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventListForSelectedDate(navController: NavController,
    selectedDate: LocalDate?,
    eventsByDate: Map<LocalDate, List<Event>>,
    modifier: Modifier = Modifier
) {
    val eventsToday = selectedDate?.let { eventsByDate[it] } ?: emptyList()

    Box(
        modifier = modifier
            .padding(16.dp)
            .heightIn(max = 500.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
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
                    EventItem(navController,event = event)
                }
            }
        }
    }
}



// Helper Function to Generate Weeks for a Month
fun getWeeksInMonth(currentMonth: YearMonth): List<List<LocalDate>> {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()

    val weeks = mutableListOf<List<LocalDate>>()
    var currentWeek = mutableListOf<LocalDate>()
    var currentDate = firstDayOfMonth

    while (currentDate <= lastDayOfMonth) {
        currentWeek.add(currentDate)
        if (currentDate.dayOfWeek == DayOfWeek.SATURDAY || currentDate == lastDayOfMonth) {
            weeks.add(currentWeek)
            currentWeek = mutableListOf()
        }
        currentDate = currentDate.plusDays(1)
    }

    return weeks
}




@Composable
fun EventChip(event: Event) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val chipColor = if (currentUser != null && event.savedUsers.contains(currentUser.uid)) {
        MaterialTheme.colorScheme.primary // Darker color for saved events
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) // Lighter color for unsaved events
    }
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
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EventItem(navController: NavController,event: Event) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Replace the description area with the EventCard
        EventCard(
            title = event.title,
            date = formatEventTime(event.startTime, event.endTime),
            location = event.location,
            photoPath = event.photo,
            onClick = {
                navController.navigate("event_details/${event.id}")
            }
        )

        // Add to Google Calendar button
        Button(
            onClick = { context.addEventToGoogleCalendar(context, event) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add to Google Calendar")
        }

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

// Helper function to format the event time
fun formatEventTime(startTime: String, endTime: String): String {
    val startDateTime = LocalDateTime.parse(startTime, eventDateTimeParser)
    val endDateTime = LocalDateTime.parse(endTime, eventDateTimeParser)
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    return "${startDateTime.format(formatter)} - ${endDateTime.format(formatter)}"
}

fun Context.addEventToGoogleCalendar(context: Context, event: Event) {
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
        putExtra(CalendarContract.Events.DESCRIPTION, event.description)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to open calendar.", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun CalendarRoute(navController: NavController) {
    val context = LocalContext.current
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseService.fetchAndSyncEvents(context) { fetchedEvents ->
            allEvents = fetchedEvents
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        CalendarPage(navController = navController, addedEvents = allEvents)
    }
}

