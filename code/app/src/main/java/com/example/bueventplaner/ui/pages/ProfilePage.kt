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

    // Variables to hold the updated names
    var updatedFirstName by remember { mutableStateOf("Loading...") }
    var updatedLastName by remember { mutableStateOf("Loading...") }

    // Fetch the user data using the fetchUserFullName function
    LaunchedEffect(Unit) {
        fetchUserFullName { fetchedFirstName, fetchedLastName ->
            updatedFirstName = fetchedFirstName
            updatedLastName = fetchedLastName
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
                ProfileHeader(
                    isEditing = isEditing,
                    updatedFirstName = updatedFirstName,
                    updatedLastName = updatedLastName,
                    onSaveChanges = { newFirstName, newLastName ->
                        FirebaseService.updateUserName(newFirstName, newLastName) { success ->
                            if (success) {
                                updatedFirstName = newFirstName
                                updatedLastName = newLastName
                            }
                        }
                    }
                )
                TabSection(navController, viewModel = viewModel)
            }
        }
    )
}

@Composable
fun ProfileHeader(
    isEditing: Boolean,
    updatedFirstName: String,
    updatedLastName: String,
    onSaveChanges: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var firstName by remember { mutableStateOf(updatedFirstName) }
    var lastName by remember { mutableStateOf(updatedLastName) }
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
                .clickable { launcher.launch("image/*") }
        ) {
            AsyncImage(
                model = "profilePics/${FirebaseAuth.getInstance().currentUser?.uid}.jpg",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(240.dp)
            )
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



