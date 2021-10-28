package se.warting.cities.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ErrorView(message: String) {
    Text(text = "Could not load cities")
    Text(text = message)
}