package com.example.bueventplaner.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.bueventplaner.R
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.ui.component.EventCard
import com.example.bueventplaner.services.FirebaseService
import com.example.bueventplaner.services.FirebaseService.fetchUserFullName
import com.example.bueventplaner.services.FirebaseService.updateProfileImageUrl
import com.example.bueventplaner.services.FirebaseService.uploadImageToFirebase
import com.example.bueventplaner.ui.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    var isEditing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Variables to hold fetched user data
    var firstName by remember { mutableStateOf("Loading...") }
    var lastName by remember { mutableStateOf("Loading...") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Fetch the user data using the fetchUserFullName function
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            fetchUserFullName { fetchedFirstName, fetchedLastName ->
                firstName = fetchedFirstName
                lastName = fetchedLastName
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Personal Homepage", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("event_list") }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isEditing = !isEditing
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login")
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_logout), contentDescription = "Logout", modifier = Modifier.size(24.dp))
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ProfileHeader(viewModel = viewModel, isEditing = isEditing, firstName = firstName, lastName = lastName, photoPath = "profilePics/${userId}.jpg")
                TabSection(navController, viewModel = viewModel)
            }
        }
    )
}

@Composable
fun ProfileHeader(viewModel: ProfileViewModel, isEditing: Boolean, firstName: String, lastName: String, photoPath: String) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Fetch the profile image URL from Firebase
    LaunchedEffect(Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val filePath = "profilePics/${userId}.jpg"
        storageReference.child(filePath).downloadUrl
            .addOnSuccessListener { url ->
                profileImageUrl = url.toString()
            }
            .addOnFailureListener { e ->
                println("Failed to fetch profile image URL: ${e.message}")
                profileImageUrl = null
            }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                uploadImageToFirebase(it) { downloadUrl ->
                    if (downloadUrl != null) {
                        updateProfileImageUrl(downloadUrl)
                        profileImageUrl = downloadUrl // Update the state with the new URL
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clickable { launcher.launch("image/*") }
        ) {
            if (profileImageUrl != null) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                // Placeholder while loading or on error
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Default Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            TextField(
                value = firstName,
                onValueChange = { /* Handle firstName editing */ },
                label = { Text("First Name") }
            )
            TextField(
                value = lastName,
                onValueChange = { /* Handle lastName editing */ },
                label = { Text("Last Name") }
            )
        } else {
            Text(
                text = "$firstName $lastName",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun TabSection(navController: NavController, viewModel: ProfileViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Reviewed") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Upcoming") }
            )
        }

        when (selectedTab) {
            0 -> EventList(navController, events = viewModel.attendedEvents)
            1 -> EventList(navController, events = viewModel.userSavedEvents)
        }
    }
}

@Composable
fun EventList(navController: NavController, events: List<Event>) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No events found.", color = MaterialTheme.colorScheme.onBackground)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(events) { event ->
                EventCard(
                    title = event.title,
                    date = "${event.startTime} - ${event.endTime}",
                    location = event.location,
                    photoPath = event.photo,
                    onClick = {
                        navController.navigate("event_details/${event.id}")
                    }
                )
            }
        }
    }
}



