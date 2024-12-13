package com.example.bueventplaner.ui.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.bueventplaner.services.FirebaseService
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.ui.component.EventCard
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.room.Room
import com.example.bueventplaner.data.repository.EventDao
import com.example.bueventplaner.data.repository.EventDatabase
import kotlinx.coroutines.launch
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context
import com.example.bueventplaner.data.model.EventEntity
import android.util.Log
import androidx.navigation.NavGraph.Companion.findStartDestination

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListPage(navController: NavController, eventDao: EventDao) {
    val context = LocalContext.current
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var filteredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Collect cached events from Room
        eventDao.getAllEvents().collect { cachedEvents ->
            allEvents = cachedEvents.map { eventEntity ->
                Event(
                    id = eventEntity.id,
                    title = eventEntity.title,
                    description = eventEntity.description,
                    location = eventEntity.location,
                    startTime = eventEntity.startTime,
                    endTime = eventEntity.endTime,
                    photo = eventEntity.photo,
                    eventUrl = eventEntity.eventUrl,
                    savedUsers = eventEntity.savedUsers
                )
            }
            filteredEvents = allEvents
            isLoading = false
        }

        // Fetch events from Firebase if online
        if (isOnline(context)) {
            FirebaseService.fetchEvents(context) { fetchedEvents ->
                allEvents = fetchedEvents
                filteredEvents = fetchedEvents
                isLoading = false

                // Update Room database
                eventDao.insertEvents(fetchedEvents.map { event ->
                    EventEntity(
                        id = event.id,
                        title = event.title,
                        description = event.description,
                        eventUrl = event.eventUrl,
                        photo = event.photo,
                        location = event.location,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        savedUsers = event.savedUsers
                    )
                })
            }
        }
    }



    LaunchedEffect(searchQuery) {
        filteredEvents = if (searchQuery.isEmpty()) {
            allEvents
        } else {
            allEvents.filter { event ->
                event.title.contains(searchQuery, ignoreCase = true) ||
                        event.description.contains(searchQuery, ignoreCase = true) ||
                        event.location.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFF0F0F0),
                modifier = Modifier.height(80.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("event_list") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                    }

                    IconButton(onClick = { navController.navigate("calendar") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar")
                        }
                    }

                    IconButton(onClick = { navController.navigate("profile") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                }
            }
        },
        modifier = Modifier.background(color = Color.White)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color.White),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFF0F0F0))
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (searchQuery.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        ImageSlider(
                            events = filteredEvents.take(4),
                            onClick = { eventId ->
                                navController.navigate("event_details/$eventId")
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Recommended For You",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(filteredEvents) { event ->
                    EventCard(
                        title = event.title,
                        date = "${event.startTime} - ${event.endTime}",
                        location = event.location,
                        photoPath = event.photo,
                        onClick = {
                            navController.navigate("event_details/${event.id}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (filteredEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No events found.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageSlider(events: List<Event>, onClick: (String) -> Unit) {
    val pagerState = rememberPagerState(0, 0f) { events.size }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) { page ->
            val event = events[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(event.id) },
                shape = RoundedCornerShape(0.dp)
            ) {
                if (event.photo.isNotEmpty()) {
                    AsyncImage(
                        model = event.photo,
                        contentDescription = "Slider Image $page",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                } else {
                    // Placeholder if URL is empty
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Image not available", color = Color.White)
                    }
                }
            }
        }

        CustomDotIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomDotIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 10.dp,
    activeColor: Color = Color.Red,
    inactiveColor: Color = Color.Gray,
    activeScale: Float = 1.1f
) {
    val pageCount = pagerState.pageCount
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val isSelected = currentPage == i
            val color by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor, label = ""
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) activeScale else 1f, label = ""
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .padding(dotSpacing)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Search", Icons.Default.Search, "event_list"),
        BottomNavItem("Calendar", Icons.Default.CalendarToday, "calendar"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    NavigationBar(
        containerColor = Color(0xFFF0F0F0)
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}






