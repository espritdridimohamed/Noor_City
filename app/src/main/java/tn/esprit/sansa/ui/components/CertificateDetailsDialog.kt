package tn.esprit.sansa.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import tn.esprit.sansa.data.models.VerificationResult
import tn.esprit.sansa.data.models.VerificationStatus
import tn.esprit.sansa.data.models.VideoCertificate
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.utils.CryptoUtils

/**
 * Dialog affichant les détails d'un certificat blockchain
 */
@Composable
fun CertificateDetailsDialog(
    certificate: VideoCertificate,
    verificationResult: VerificationResult,
    onDismiss: () -> Unit,
    onExport: () -> Unit = {},
    onVerify: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                CertificateHeader(
                    verificationResult = verificationResult,
                    onDismiss = onDismiss
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Badge
                    VerificationStatusBadge(verificationResult)

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Certificate Details
                    CertificateInfoSection(certificate)

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Blockchain Details
                    BlockchainInfoSection(certificate, verificationResult)

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Hash Details
                    HashInfoSection(certificate)

                    Spacer(Modifier.height(8.dp))

                    // Action Buttons
                    CertificateActions(
                        onVerify = onVerify,
                        onExport = onExport,
                        verificationResult = verificationResult
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificateHeader(
    verificationResult: VerificationResult,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                color = verificationResult.statusColor.copy(alpha = 0.15f)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = verificationResult.statusColor,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        "Certificat Blockchain",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Preuve cryptographique",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Fermer")
            }
        }
    }
}

@Composable
private fun VerificationStatusBadge(verificationResult: VerificationResult) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = verificationResult.statusColor.copy(
            alpha = if (verificationResult.isValid) alpha * 0.15f else 0.15f
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            verificationResult.statusColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = verificationResult.statusIcon,
                contentDescription = null,
                tint = verificationResult.statusColor,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when (verificationResult.status) {
                        VerificationStatus.AUTHENTIC -> "✓ Certificat Authentique"
                        VerificationStatus.TAMPERED -> "⚠ Vidéo Modifiée"
                        VerificationStatus.NOT_FOUND -> "✗ Certificat Non Trouvé"
                        VerificationStatus.CHAIN_BROKEN -> "✗ Chaîne Corrompue"
                        VerificationStatus.PENDING -> "⏳ Vérification en cours..."
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = verificationResult.statusColor
                )
                Text(
                    verificationResult.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CertificateInfoSection(certificate: VideoCertificate) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "📋 Informations du Certificat",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        InfoItem(
            icon = Icons.Default.Videocam,
            label = "Caméra",
            value = certificate.cameraId
        )

        InfoItem(
            icon = Icons.Default.LocationOn,
            label = "Emplacement",
            value = certificate.cameraLocation
        )

        InfoItem(
            icon = Icons.Default.Schedule,
            label = "Horodatage",
            value = certificate.timestampFormatted
        )

        InfoItem(
            icon = Icons.Default.CalendarMonth,
            label = "Date de création",
            value = CryptoUtils.formatTimestamp(certificate.createdAt)
        )
    }
}

@Composable
private fun BlockchainInfoSection(
    certificate: VideoCertificate,
    verificationResult: VerificationResult
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "⛓️ Informations Blockchain",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        InfoItem(
            icon = Icons.Default.Tag,
            label = "Numéro de Bloc",
            value = "#${certificate.blockNumber}"
        )

        InfoItem(
            icon = Icons.Default.Link,
            label = "Hash Précédent",
            value = CryptoUtils.shortHash(certificate.previousBlockHash, 6),
            monospace = true
        )

        InfoItem(
            icon = Icons.Default.VerifiedUser,
            label = "Statut de Vérification",
            value = if (verificationResult.chainIntegrity) "✓ Chaîne Intègre" else "✗ Chaîne Brisée",
            valueColor = if (verificationResult.chainIntegrity) NoorGreen else NoorRed
        )
    }
}

@Composable
private fun HashInfoSection(certificate: VideoCertificate) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "🔐 Empreinte Cryptographique",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Hash SHA-256",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    certificate.videoHash,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }

        Text(
            "ℹ️ Cette empreinte est unique et garantit l'authenticité de l'enregistrement. Toute modification de la vidéo changerait cette valeur.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    monospace: Boolean = false,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CertificateActions(
    onVerify: () -> Unit,
    onExport: () -> Unit,
    verificationResult: VerificationResult
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Verify Button
        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NoorBlue
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Revérifier l'Intégrité", fontSize = 15.sp)
        }

        // Export Button
        OutlinedButton(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth(),
            enabled = verificationResult.isValid
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Exporter pour Tribunal", fontSize = 15.sp)
        }

        Text(
            "💡 L'export génère un PDF avec toutes les preuves cryptographiques pour usage légal.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Badge compact de certification pour affichage dans les listes
 */
@Composable
fun CertificationBadge(
    hasCertificate: Boolean,
    verified: Boolean,
    modifier: Modifier = Modifier
) {
    if (hasCertificate) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(6.dp),
            color = if (verified) NoorGreen.copy(alpha = 0.15f) else NoorAmber.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (verified) Icons.Default.Verified else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (verified) NoorGreen else NoorAmber,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    if (verified) "Certifié" else "En attente",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (verified) NoorGreen else NoorAmber
                )
            }
        }
    }
}
