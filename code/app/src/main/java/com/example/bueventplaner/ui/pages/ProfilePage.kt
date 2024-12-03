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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController) {
    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("Loading...") }
    var lastName by remember { mutableStateOf("Loading...") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userSavedEvents = remember { mutableStateListOf<Event>() }

    // Fetch the user data using the fetchUserFullName function
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            fetchUserFullName { fetchedFirstName, fetchedLastName ->
                firstName = fetchedFirstName
                lastName = fetchedLastName
            }

            FirebaseService.fetchUserSavedEvents(userSavedEvents)
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Personal Homepage", fontSize = 20.sp) },
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
                    IconButton(onClick = { isEditing = !isEditing }) {
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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Logout",
                            modifier = Modifier.size(24.dp)
                        )
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
                ProfileHeader(
                    isEditing = isEditing,
                    updatedFirstName = firstName,
                    updatedLastName = lastName,
                    onSaveChanges = { newFirstName, newLastName ->
                        FirebaseService.updateUserName(newFirstName, newLastName) { success ->
                            if (success) {
                                firstName = newFirstName
                                lastName = newLastName
                            }
                        }
                    },
                    onEditModeChange = { isEditing = it }
                )
                TabSection(navController, userSavedEvents)
            }
        }
    )
}


@Composable
fun ProfileHeader(
    isEditing: Boolean,
    updatedFirstName: String,
    updatedLastName: String,
    onSaveChanges: (String, String) -> Unit,
    onEditModeChange: (Boolean) -> Unit // New parameter for editing state control
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    var firstName by remember { mutableStateOf(updatedFirstName) }
    var lastName by remember { mutableStateOf(updatedLastName) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

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
    
    // Update local state when fetched data changes
    LaunchedEffect(updatedFirstName, updatedLastName) {
        firstName = updatedFirstName
        lastName = updatedLastName
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                uploadImageToFirebase(it) { downloadUrl ->
                    if (downloadUrl != null) {
                        updateProfileImageUrl(downloadUrl)
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
                .clickable { launcher.launch("image/*") } // Open file picker
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
                onValueChange = { firstName = it },
                label = { Text("First Name") }
            )
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onSaveChanges(firstName, lastName)
                    onEditModeChange(false) // Exit editing mode after saving
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Save Changes")
            }
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
fun TabSection(navController: NavController, userSavedEvents: MutableList<Event>) {
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
            0 -> EventList(navController, events = userSavedEvents)
            1 -> EventList(navController, events = userSavedEvents)
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



