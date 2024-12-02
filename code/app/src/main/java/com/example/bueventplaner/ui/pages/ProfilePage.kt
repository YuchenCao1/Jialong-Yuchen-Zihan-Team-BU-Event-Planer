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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.ui.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavHostController, viewModel: ProfileViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val tabState = remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {
                        if (viewModel.isEditing.value) {
                            coroutineScope.launch { viewModel.saveUserData() }
                        }
                        viewModel.isEditing.value = !viewModel.isEditing.value
                    }) {
                        Icon(
                            imageVector = if (viewModel.isEditing.value) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login")
                    }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(viewModel = viewModel)
                Spacer(modifier = Modifier.height(16.dp))

                // Tab Section for "Saved" and "Attended" events
                TabRow(
                    selectedTabIndex = tabState.value,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    listOf("Saved", "Attended").forEachIndexed { index, title ->
                        Tab(
                            selected = tabState.value == index,
                            onClick = { tabState.value = index },
                            text = { Text(title, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                        )
                    }
                }

                Divider()

                // Tab Content
                when (tabState.value) {
                    0 -> EventList(events = viewModel.userSavedEvents)
                    1 -> EventList(events = viewModel.attendedEvents)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(viewModel: ProfileViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                viewModel.uploadImageToFirebase(it) { downloadUrl ->
                    if (downloadUrl != null) {
                        println("Image uploaded: $downloadUrl")
                    } else {
                        println("Image upload failed.")
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val profileImagePainter = rememberImagePainter(data = viewModel.profileImageUrl.value)
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clickable { launcher.launch("image/*") }
        ) {
            Image(
                painter = profileImagePainter,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isUploading.value) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isEditing.value) {
            TextField(
                value = viewModel.firstName.value,
                onValueChange = { viewModel.firstName.value = it },
                label = { Text("First Name") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.lastName.value,
                onValueChange = { viewModel.lastName.value = it },
                label = { Text("Last Name") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        } else {
            Text(
                text = "${viewModel.firstName.value} ${viewModel.lastName.value}",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${viewModel.points.value} Points",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EventList(events: List<Event>) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No events found.", color = MaterialTheme.colorScheme.onBackground)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(events) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
