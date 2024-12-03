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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.ui.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.bueventplaner.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    var isEditing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                ProfileHeader(viewModel = viewModel, isEditing = isEditing)
                TabSection(viewModel = viewModel)
            }
        }
    )
}

@Composable
fun ProfileHeader(viewModel: ProfileViewModel, isEditing: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                viewModel.uploadImageToFirebase(it) { downloadUrl ->
                    if (downloadUrl != null) {
                        viewModel.updateProfileImageUrl(downloadUrl)
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
        val profileImagePainter = rememberAsyncImagePainter(viewModel.profileImageUrl.value)
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                .clickable { launcher.launch("image/*") } // 打开文件选择器
        ) {
            Image(
                painter = profileImagePainter,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            TextField(
                value = viewModel.firstName.value,
                onValueChange = { viewModel.firstName.value = it },
                label = { Text("First Name") }
            )
            TextField(
                value = viewModel.lastName.value,
                onValueChange = { viewModel.lastName.value = it },
                label = { Text("Last Name") }
            )
        } else {
            Text(
                text = "${viewModel.firstName.value} ${viewModel.lastName.value}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TabSection(viewModel: ProfileViewModel) {
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
            0 -> EventList(events = viewModel.attendedEvents)
            1 -> EventList(events = viewModel.userSavedEvents)
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
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}