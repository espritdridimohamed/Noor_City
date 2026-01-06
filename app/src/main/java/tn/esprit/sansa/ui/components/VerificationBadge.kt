package tn.esprit.sansa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.NoorBlue

enum class BadgeStatus {
    VERIFIED,
    PROCESSING,
    UNSECURED
}

@Composable
fun VerificationBadge(
    status: BadgeStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, contentColor, icon, text) = when (status) {
        BadgeStatus.VERIFIED -> BadgeConfig(
            Color(0xFFE8F5E9), // Light Green
            Color(0xFF2E7D32), // Dark Green
            Icons.Default.CheckCircle,
            "Certifié Blockchain"
        )
        BadgeStatus.PROCESSING -> BadgeConfig(
            Color(0xFFFFF3E0), // Light Orange
            Color(0xFFEF6C00), // Dark Orange
            Icons.Default.Lock,
            "Sécurisation..."
        )
        BadgeStatus.UNSECURED -> BadgeConfig(
            Color(0xFFF5F5F5), // Light Grey
            Color(0xFF757575), // Dark Grey
            Icons.Default.Warning,
            "Non Sécurisé"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, contentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class BadgeConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val text: String
)
