package tn.esprit.sansa.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import tn.esprit.sansa.ui.screens.models.Streetlight
import tn.esprit.sansa.ui.theme.*
import tn.esprit.sansa.ui.viewmodels.NoorChargeViewModel
import tn.esprit.sansa.ui.viewmodels.PaymentStatus

@Composable
fun NoorChargeDialog(
    streetlight: Streetlight,
    onDismiss: () -> Unit,
    viewModel: NoorChargeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val energy by viewModel.energyConsumed.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    
    var selectedDuration by remember { mutableStateOf(30f) } // Minutes
    val totalPrice = remember(selectedDuration) { (selectedDuration / 15.0) } // 15 min = 1 TND

    Dialog(
        onDismissRequest = { if (!isCharging) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Noor Charge",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoorBlue
                        )
                        Text(
                            "Station ${streetlight.id}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isCharging) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedContent(
                    targetState = paymentStatus,
                    label = "ChargeContent"
                ) { status ->
                    when {
                        isCharging -> ChargingSessionUI(energy, timeRemaining, onStop = { viewModel.stopChargingSession(streetlight) })
                        status == PaymentStatus.COLLECTING_CARD_INFO -> StripeCardInputUI(
                            amount = totalPrice,
                            onPay = { card -> viewModel.processStripePayment(totalPrice, card, {}) },
                            onBack = { viewModel.resetPayment() }
                        )
                        status == PaymentStatus.PENDING -> PaymentPendingUI()
                        status == PaymentStatus.SUCCESS -> PaymentSuccessUI(
                            duration = selectedDuration.toInt(),
                            onStart = { viewModel.startChargingSession(streetlight, selectedDuration.toInt()) }
                        )
                        status == PaymentStatus.FAILED -> PaymentFailedUI(onRetry = { viewModel.resetPayment() })
                        else -> InitialPaymentUI(
                            duration = selectedDuration,
                            price = totalPrice,
                            onDurationChange = { selectedDuration = it },
                            onPay = { viewModel.initiatePayment() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialPaymentUI(
    duration: Float,
    price: Double,
    onDurationChange: (Float) -> Unit,
    onPay: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NoorBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ElectricBolt, null, tint = NoorBlue, modifier = Modifier.size(56.dp))
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("Durée de recharge", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text("${duration.toInt()} minutes", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NoorBlue)
        
        Spacer(Modifier.height(8.dp))
        
        Slider(
            value = duration,
            onValueChange = onDurationChange,
            valueRange = 15f..120f,
            steps = 6, // Par pas de 15 min (15, 30, 45, 60, 75, 90, 105, 120)
            colors = SliderDefaults.colors(
                thumbColor = NoorBlue,
                activeTrackColor = NoorBlue,
                inactiveTrackColor = NoorBlue.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total à payer", fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%.3f TND", price),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NoorIndigo
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6772E5)), // Stripe Blurple
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.CreditCard, null)
            Spacer(Modifier.width(8.dp))
            Text("Payer avec Stripe")
        }
    }
}

@Composable
private fun StripeCardInputUI(
    amount: Double,
    onPay: (String) -> Unit,
    onBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Infos de paiement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Visual Card Representation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6772E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(Modifier.padding(20.dp).fillMaxSize()) {
                Icon(Icons.Default.CreditCard, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp).align(Alignment.BottomEnd))
                Column {
                    Text("STRIPE SECURE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        if (cardNumber.isEmpty()) "•••• •••• •••• ••••" else cardNumber.padEnd(16, '•').chunked(4).joinToString(" "),
                        color = Color.White,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("VALID THRU", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
                            Text(if (expiry.isEmpty()) "MM/YY" else expiry, color = Color.White, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("CVV", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
                            Text(if (cvv.isEmpty()) "•••" else "•••", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 16) cardNumber = it.filter { char -> char.isDigit() } },
            label = { Text("Numéro de carte") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CreditCard, null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = expiry,
                onValueChange = { if (it.length <= 5) expiry = it },
                label = { Text("MM/YY") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 3) cvv = it.filter { char -> char.isDigit() } },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onPay(cardNumber) },
            modifier = Modifier.fillMaxWidth(),
            enabled = cardNumber.length == 16 && expiry.length >= 4 && cvv.length == 3,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6772E5)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Payer ${String.format("%.3f", amount)} TND")
        }
    }
}

@Composable
private fun PaymentPendingUI() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), color = NoorBlue)
        Spacer(Modifier.height(24.dp))
        Text("Sécurisation de la transaction Stripe...")
    }
}

@Composable
private fun PaymentSuccessUI(duration: Int, onStart: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, null, tint = NoorGreen, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Paiement validé !", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Session de $duration min prête.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NoorGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Démarrer la charge")
        }
    }
}

@Composable
private fun PaymentFailedUI(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Error, null, tint = NoorRed, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Échec du paiement", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun ChargingSessionUI(energy: Double, timeRemaining: Int, onStop: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChargingAnimation(modifier = Modifier.size(150.dp))
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = String.format("%.1f Wh", energy),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = NoorBlue
        )
        Text("Énergie délivrée", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(16.dp))
        
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Temps restant", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoorRed),
            border = BorderStroke(1.dp, NoorRed)
        ) {
            Text("Arrêter la charge")
        }
    }
}

@Composable
private fun ChargingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = NoorBlue.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = NoorBlue,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Icon(
            Icons.Default.ElectricBolt,
            null,
            tint = NoorBlue,
            modifier = Modifier.size(64.dp)
        )
    }
}
