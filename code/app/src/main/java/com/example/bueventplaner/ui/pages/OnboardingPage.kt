package com.example.bueventplaner.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

@Composable
fun OnboardingPage(navController: NavController) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.onboarding1,
            title = "Attend events",
            description = "When you attend an event, make sure to check in with our app! A pop-up will appear with the options to check-in with a photo for 25 points, check-in without a photo for 20 points, or to let us know you are not attending.",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding2,
            title = "Collect Stickers",
            description = "Every event you attend can give you up to 2 stickers. Different events mean more chances for new stickers. Try to collect all 9 stickers and complete your collection! A completed collection earns you 5 points.",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding3,
            title = "Make new friends",
            description = "Friends can add you with your unique QR code, or you can add them by scanning theirs! You can check out their public reviews, and they can check yours.",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding4,
            title = "Leave Feedback",
            description = "After attending an event, leave a review for 5 points! Tell us about how you felt about it, any feedback, or anything at all! Reviews can be private or can be made public on your profile page.",
            color = Color.White
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding5,
            title = "Your Passport",
            description = "Your passport is your personal catalog of every event that you have attended with us. Look back on your past attendances, all of your reviews, and your sticker collection.",
            color = Color.White
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val pageCount = pages.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.width(120.dp)
                    )
                },
                backgroundColor = MaterialTheme.colors.background
            )
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = MaterialTheme.colors.background,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text("Skip", fontSize = 18.sp)
                        }
                        TextButton(onClick = {
                            if (currentPage < pageCount - 1) {
                                currentPage++
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
                count = pageCount,
                state = rememberPagerState(currentPage)
            ) { page ->
                OnboardingPageContent(data = pages[page])
            }

            // Dots Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .background(
                                color = if (isSelected) Color.Red else Color.Gray,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
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
                .fillMaxWidth(0.8f)
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = data.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = data.description,
            fontSize = 16.sp,
            color = Color.Gray,
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
