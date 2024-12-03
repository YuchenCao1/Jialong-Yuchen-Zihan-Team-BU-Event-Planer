package com.example.bueventplaner.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bueventplaner.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPage(navController: NavController) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.onboarding1,
            title = "Attend events",
            description = "When you attend an event, make sure to check in with our app! A pop-up will appear with the options to check-in with a photo, or to let us know you are not attending.",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding2,
            title = "Make new friends",
            description = "Friends can add you with your unique User ID. You can check out their public reviews, and they can check yours. ",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding3,
            title = "Leave Feedback",
            description = "After attending an event, you can leave a review! Tell us about how you felt about it, any feedback, or anything at all! Reviews can be private or can be made public on your profile page.",
            color = Color.White
        ),
    )

    val pagerState = rememberPagerState(initialPage = 0)
    val pageCount = pages.size
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text("Skip", fontSize = 18.sp)
                        }
                        TextButton(onClick = {
                            if (pagerState.currentPage < pageCount - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                navController.navigate("login")
                            }
                        }) {
                            Text("Next", fontSize = 18.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                count = pages.size,
                state = pagerState
            ) { page ->
                OnboardingPageContent(data = pages[page])
            }
        }
    }
}

@Composable
fun OnboardingPageContent(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = data.imageRes),
            contentDescription = data.title,
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(300.dp)
        )
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            text = data.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = data.description,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String,
    val color: Color
)