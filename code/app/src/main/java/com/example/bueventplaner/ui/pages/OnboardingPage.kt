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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bueventplaner.R
import kotlinx.coroutines.launch
import android.content.res.Configuration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPage(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.onboarding1,
            title = "Attend events",
            description = "When you attend an event, make sure to check in with our app! A pop-up will appear with the options to check-in with a photo, or to let us know you are not attending.",
            color = Color.White,
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
                    .fillMaxHeight(0.09f),
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
                                .fillMaxHeight(0.9f)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text("Skip", fontSize = (screenWidth.value * 0.04f).coerceAtMost(12f).sp)
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
                            Text("Next", fontSize = (screenWidth.value * 0.04f).coerceAtMost(12f).sp)
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
                OnboardingPageContent(data = pages[page], screenWidth = screenWidth, screenHeight = screenWidth)
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    data: OnboardingPageData,
    screenWidth: Dp,
    screenHeight: Dp
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    if (isPortrait) {
        // Portrait Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height((screenHeight.value * 0.09f).dp))
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = data.title,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height((screenHeight.value * 0.8f).coerceAtMost(250f).dp)
            )
            Spacer(modifier = Modifier.height((screenHeight.value * 0.15f).dp))
            Text(
                text = data.title,
                fontSize = (screenWidth.value * 0.06f).sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height((screenHeight.value * 0.15f).dp))
            Text(
                text = data.description,
                fontSize = (screenWidth.value * 0.04f).sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Landscape Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = data.title,
                modifier = Modifier
                    .weight(1f)
                    .height((screenHeight.value * 0.6f).coerceAtMost(200f).dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = data.title,
                    fontSize = (screenWidth.value * 0.05f).coerceAtMost(18f).sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data.description,
                    fontSize = (screenWidth.value * 0.035f).coerceAtMost(12f).sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}


data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String,
    val color: Color
)
