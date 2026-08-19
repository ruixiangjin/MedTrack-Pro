package com.medtrackpro.demo.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val tabs = listOf(
        MedTrackRoutes.HOME to "Home",
        MedTrackRoutes.SYMPTOMS to "Symptoms",
        MedTrackRoutes.MEDCOACH to "MedCoach",
        MedTrackRoutes.SETTINGS to "Settings"
    )

    Surface(color = colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            tabs.forEach { (route, label) ->
                val isSelected = route == currentRoute
                Button(
                    onClick = {
                        if (!isSelected) {
                            onNavigate(route)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            colorScheme.primary
                        } else {
                            colorScheme.secondary
                        }
                    )
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) {
                            colorScheme.onPrimary
                        } else {
                            colorScheme.onSecondary
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
