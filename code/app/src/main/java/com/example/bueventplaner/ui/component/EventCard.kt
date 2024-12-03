package com.example.bueventplaner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EventCard(
    title: String,
    date: String,
    location: String,
    photoPath: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (photoPath.isNotEmpty()) {
                AsyncImage(
                    model = photoPath,
                    contentDescription = "Event Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = date, style = MaterialTheme.typography.bodySmall)
                Text(text = location, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
