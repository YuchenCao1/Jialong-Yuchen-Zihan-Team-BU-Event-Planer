package com.example.bueventplaner.ui.pages

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.data.model.EventEntity
import com.example.bueventplaner.data.repository.EventDao
import com.example.bueventplaner.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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