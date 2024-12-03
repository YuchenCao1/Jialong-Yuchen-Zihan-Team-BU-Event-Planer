//package com.example.bueventplaner.ui.pages
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import coil.compose.rememberAsyncImagePainter
//import com.example.bueventplaner.data.model.Event
//import com.example.bueventplaner.data.model.User
//import com.example.bueventplaner.services.FirebaseService
//import com.example.bueventplaner.R
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProfilePage(navController: NavController, username: String) {
//    var isEditing by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Personal Homepage", fontSize = 20.sp) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigate("event_list") }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_back),
//                            contentDescription = "Back",
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { isEditing = !isEditing }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_settings),
//                            contentDescription = "Edit",
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                    IconButton(onClick = {
//                        FirebaseAuth.getInstance().signOut()
//                        navController.navigate("login")
//                    }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_logout),
//                            contentDescription = "Logout",
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//            )
//        },
//        content = { padding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding)
//            ) {
//                ProfileHeader(username = username, isEditing = isEditing)
//                //TabSection(username = username)
//            }
//        }
//    )
//}
//
//@Composable
//fun ProfileHeader(username: String, isEditing: Boolean) {
//    var userProfile by remember { mutableStateOf<User?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    val coroutineScope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    LaunchedEffect(Unit) {
//        FirebaseService.fetchUserByUsername(username) { user ->
//            userProfile = user
//            isLoading = false
//        }
//    }
//
//    if (isLoading) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator()
//        }
//    } else {
//        userProfile?.let { user ->
//            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//                uri?.let {
//                    coroutineScope.launch { // 修复：协程中调用挂起函数
//                        val downloadUrl = FirebaseService.uploadUserProfileImage(it)
//                        if (downloadUrl != null) {
//                            FirebaseService.updateUserProfileImageUrl(username, downloadUrl) {
//                                userProfile = userProfile?.copy(userImage = downloadUrl)
//                            }
//                        }
//                    }
//                }
//            }
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                val profileImagePainter = rememberAsyncImagePainter(user.userImage)
//                Box(
//                    modifier = Modifier
//                        .size(100.dp)
//                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
//                        .clickable { launcher.launch("image/*") }
//                ) {
//                    Image(
//                        painter = profileImagePainter,
//                        contentDescription = "Profile Picture",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                if (isEditing) {
//                    TextField(
//                        value = user.firstName,
//                        onValueChange = { newValue ->
//                            userProfile = user.copy(firstName = newValue)
//                            FirebaseService.updateUserField(username, "firstName", newValue)
//                        },
//                        label = { Text("First Name") }
//                    )
//                    TextField(
//                        value = user.lastName,
//                        onValueChange = { newValue ->
//                            userProfile = user.copy(lastName = newValue)
//                            FirebaseService.updateUserField(username, "lastName", newValue)
//                        },
//                        label = { Text("Last Name") }
//                    )
//                } else {
//                    Text(
//                        text = "${user.firstName.ifEmpty { "First Name" }} " +
//                                user.lastName.ifEmpty { "Last Name" }",
//                                fontSize = 20.sp
//                    )
//                }
//            }
//        } ?: run {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = "User not found.")
//            }
//        }
//    }
//}
//
//
//
////@Composable
////fun TabSection(username: String) {
////    var selectedTab by remember { mutableIntStateOf(0) }
////    var userEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
////
////    LaunchedEffect(Unit) {
////        FirebaseService.fetchUserEvents(username) { events ->
////            userEvents = events
////        }
////    }
////
////    Column {
////        TabRow(
////            selectedTabIndex = selectedTab,
////            containerColor = MaterialTheme.colorScheme.surface
////        ) {
////            Tab(
////                selected = selectedTab == 0,
////                onClick = { selectedTab = 0 },
////                text = { Text("Reviewed") }
////            )
////            Tab(
////                selected = selectedTab == 1,
////                onClick = { selectedTab = 1 },
////                text = { Text("Upcoming") }
////            )
////        }
////
////        when (selectedTab) {
////            0 -> EventList(events = userEvents.filter { it.savedUsers.contains(username) })
////            1 -> EventList(events = userEvents.filter { !it.savedUsers.contains(username) })
////        }
////    }
////}
//
//
//@Composable
//fun EventList(events: List<Event>) {
//    if (events.isEmpty()) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "No events found.",
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        }
//    } else {
//        LazyColumn(contentPadding = PaddingValues(16.dp)) {
//            items(events) { event ->
//                EventCard(event = event)
//            }
//        }
//    }
//}
//
//@Composable
//fun EventCard(event: Event) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
//            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}
