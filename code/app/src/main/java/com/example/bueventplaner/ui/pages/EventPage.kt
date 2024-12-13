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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsView(navController: NavController, eventId: String?, eventDao: EventDao) {
    val context = LocalContext.current
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isadded by remember { mutableStateOf(false) }
    val TAG = "MyDebugTag"

    // Fetch event details and check add status
    LaunchedEffect(eventId) {
        eventId?.let { id ->
            // Collect the event from Room database
            eventDao.getEventById(id).collect { cachedEvent ->
                event = cachedEvent?.let { entity ->
                    Event(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        eventUrl = entity.eventUrl,
                        photo = entity.photo,
                        location = entity.location,
                        startTime = entity.startTime,
                        endTime = entity.endTime,
                        savedUsers = entity.savedUsers
                    )
                }

                if(isOnline(context)) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val userRef = Firebase.database.reference.child("users").child(userId)
                        userRef.child("savedEvents").get().addOnSuccessListener { snapshot ->
                            // Use explicit type casting to avoid type inference issues
                            val savedEvents = snapshot.value as? List<String> ?: emptyList()
                            isadded = eventId in savedEvents
                            Log.d(TAG, "Success in ED: ${isadded}")
                        }.addOnFailureListener {
                            Log.d(TAG, "Failed to fetch saved events in ED: ${it.message}")
                        }
                    }
                }
                isLoading = event == null // Show loading indicator if no cached event found
            }

            // If online, fetch latest event from Firebase
            if (event == null && isOnline(context)) {
                Log.d(TAG, "111")
                FirebaseService.fetchEventById(context, id) { fetchedEvent ->
                    event = fetchedEvent
                    isLoading = false
                    Log.d(TAG, "222")

                    // Update Room database with the fetched event
                    fetchedEvent?.let { entity ->
                        eventDao.insertEvents(
                            listOf(
                                EventEntity(
                                    id = entity.id,
                                    title = entity.title,
                                    description = entity.description,
                                    eventUrl = entity.eventUrl,
                                    photo = entity.photo,
                                    location = entity.location,
                                    startTime = entity.startTime,
                                    endTime = entity.endTime,
                                    savedUsers = entity.savedUsers
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event?.title ?: "Event Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share Action */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (event != null) {
            val eventDetails = event!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    // Event Image
                    if (eventDetails.photo.isNotEmpty()) {
                        AsyncImage(
                            model = eventDetails.photo, // Full image URL from Firebase Storage
                            contentDescription = "Event Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    } else {
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
                item {
                    // Event Details in a Card with Border
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Event Title
                            Text(
                                text = eventDetails.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Event Dates
                            Text(
                                text = "Date: ${eventDetails.startTime} - ${eventDetails.endTime}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Event Description
                            Text(
                                text = "Description:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = eventDetails.description,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Event Location
                            Text(
                                text = "Location:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = eventDetails.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Event Location with Map
                            EventLocationCardWithMap(
                                title = eventDetails.title,
                                address = eventDetails.location,
                                latitude = 42.3601,
                                longitude = -71.0589
                            )

                            // Event Link
                            Text(
                                text = "Learn More:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = eventDetails.eventUrl,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue, textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable {
                                    val url = eventDetails.eventUrl
                                    if (url.isNotEmpty() && Uri.parse(url).isAbsolute) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(url)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
                item {
                    // Display add Button
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (!isOnline(context)) {
                                Toast.makeText(context, "You are offline. Please check your network connection.", Toast.LENGTH_SHORT).show()
                            } else {
                                if (isadded) {
                                    FirebaseService.removeEventForUser(context, eventId!!) { isSuccess ->
                                        if (isSuccess) {
                                            Toast.makeText(
                                                context,
                                                "Event unregistered successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isadded = false
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to unregister event. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    FirebaseService.addEventForUser(context, eventId!!) { isSuccess ->
                                        if (isSuccess) {
                                            Toast.makeText(
                                                context,
                                                "Event registered successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isadded = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to register event. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isadded) Color.Gray else Color.Red)
                    ) {
                        Text(
                            text = if (isadded) "Remove from Calendar" else "Add to Calendar",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found or failed to load.")
            }
        }
    }
}




@Composable
fun EventLocationCardWithMap(
    title: String,               //  "Mindshop Online Classroom"
    address: String,             // "Boston, MA 00000"
    latitude: Double,            //  42.3601
    longitude: Double            //  -71.0589
) {

    val locationLatLng = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationLatLng, 13f) // 默认缩放级别
    }

    Column(
        modifier = Modifier
            .padding(6.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), MaterialTheme.shapes.medium)
    ) {

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = locationLatLng),
                    title = title,
                    snippet = address
                )
            }

        }


    }
}
