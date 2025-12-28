package tn.esprit.sansa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryBlue = Color(0xFF2563EB)
private val LightSurface = Color(0xFFF6F8FC)

enum class HistoryTab(val label: String) {
    LIGHT("Lumière"),
    MOTION("Mouvement"),
    TEMP("Température")
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(HistoryTab.LIGHT) }

    Scaffold(
        containerColor = LightSurface,
        topBar = {
            Surface(
                color = PrimaryBlue,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        "Historique",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Analysez les données de vos capteurs",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightSurface)
                .verticalScroll(rememberScrollState())
        ) {
            // Tabs
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color(0xFFEFF2F7),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HistoryTab.values().forEach { tab ->
                        TabButton(
                            text = tab.label,
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Données de ${selectedTab.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    // Simple stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Moyenne",
                            value = when (selectedTab) {
                                HistoryTab.LIGHT -> "682 lux"
                                HistoryTab.MOTION -> "1.6 /h"
                                HistoryTab.TEMP -> "20.4°C"
                            },
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Maximum",
                            value = when (selectedTab) {
                                HistoryTab.LIGHT -> "998 lux"
                                HistoryTab.MOTION -> "4 /h"
                                HistoryTab.TEMP -> "23.0°C"
                            },
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Minimum",
                            value = when (selectedTab) {
                                HistoryTab.LIGHT -> "123 lux"
                                HistoryTab.MOTION -> "0 /h"
                                HistoryTab.TEMP -> "18.0°C"
                            },
                            color = Color(0xFFF97316),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (selected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 12.dp),
            color = if (selected) PrimaryBlue else Color(0xFF6B7280),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
