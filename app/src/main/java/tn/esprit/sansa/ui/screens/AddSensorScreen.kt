// AddSensorScreen.kt – VERSION ALTERNATIVE (Décembre 2025)
package tn.esprit.sansa.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.ui.theme.SansaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Palette Noor commune
private val NoorBlue = Color(0xFF1E40AF)
private val NoorGreen = Color(0xFF10B981)
private val NoorAmber = Color(0xFFF59E0B)
private val NoorRed = Color(0xFFEF4444)
private val NoorPurple = Color(0xFF8B5CF6)
private val NoorCyan = Color(0xFF06B6D4)

// Liste fictive de lampadaires
private val mockStreetlights = listOf(
    "L001 - Lampadaire #001",
    "L002 - Lampadaire #002",
    "L003 - Lampadaire #003",
    "L004 - Lampadaire #004",
    "L005 - Lampadaire #005",
    "L006 - Lampadaire #006",
    "L007 - Lampadaire #007",
    "L008 - Lampadaire #008"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSensorScreen(onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf<SensorType?>(null) }
    var selectedStreetlight by remember { mutableStateOf("") }
    var initialValue by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableStateOf(100f) }
    var selectedStatus by remember { mutableStateOf(SensorStatus.ACTIVE) }

    var showTypeError by remember { mutableStateOf(false) }
    var showStreetlightError by remember { mutableStateOf(false) }
    var showValueError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showSuccessAnimation by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Ajouter un capteur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Configurez un nouveau capteur intelligent",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FormSection(
                    title = "Type de capteur",
                    icon = Icons.Default.Category,
                    isError = showTypeError
                ) {
                    SensorTypeSelector(
                        selectedType = selectedType,
                        onTypeSelected = {
                            selectedType = it
                            showTypeError = false
                        }
                    )
                    AnimatedVisibility(visible = showTypeError) {
                        Text(
                            "Veuillez sélectionner un type de capteur",
                            color = NoorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                FormSection(
                    title = "Lampadaire associé",
                    icon = Icons.Default.Lightbulb,
                    isError = showStreetlightError
                ) {
                    StreetlightDropdown(
                        selectedStreetlight = selectedStreetlight,
                        onStreetlightSelected = {
                            selectedStreetlight = it
                            showStreetlightError = false
                        },
                        isError = showStreetlightError
                    )
                    AnimatedVisibility(visible = showStreetlightError) {
                        Text(
                            "Veuillez sélectionner un lampadaire",
                            color = NoorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                FormSection(
                    title = "Valeur initiale",
                    icon = Icons.Default.Speed,
                    isError = showValueError
                ) {
                    CustomTextField(
                        value = initialValue,
                        onValueChange = {
                            initialValue = it
                            showValueError = false
                        },
                        label = "Valeur",
                        placeholder = selectedType?.let { "Ex: ${when (it) {
                            SensorType.LIGHT -> "750"
                            SensorType.TEMPERATURE -> "22"
                            SensorType.HUMIDITY -> "65"
                            SensorType.MOTION -> "5"
                            SensorType.POWER -> "120"
                        }}" } ?: "Entrez une valeur",
                        suffix = selectedType?.unit ?: "",
                        isError = showValueError,
                        keyboardType = KeyboardType.Decimal
                    )
                    AnimatedVisibility(visible = showValueError) {
                        Text(
                            "Veuillez entrer une valeur numérique valide",
                            color = NoorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                FormSection(
                    title = "Niveau de batterie",
                    icon = Icons.Default.BatteryFull
                ) {
                    BatterySlider(
                        batteryLevel = batteryLevel,
                        onBatteryChange = { batteryLevel = it }
                    )
                }

                FormSection(
                    title = "Statut",
                    icon = Icons.Default.CheckCircle
                ) {
                    StatusChips(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ActionButtons(
                    onCancel = onBack,
                    onAdd = {
                        var hasError = false

                        if (selectedType == null) {
                            showTypeError = true
                            hasError = true
                        }
                        if (selectedStreetlight.isEmpty()) {
                            showStreetlightError = true
                            hasError = true
                        }
                        if (initialValue.isEmpty() || initialValue.toFloatOrNull() == null) {
                            showValueError = true
                            hasError = true
                        }

                        if (!hasError) {
                            showSuccessAnimation = true
                            scope.launch {
                                delay(1200)
                                onBack()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut()
            ) {
                SuccessAnimation()
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    isError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isError) BorderStroke(2.dp, NoorRed) else null
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) NoorRed else NoorBlue,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    suffix: String = "",
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isError) NoorRed else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (isError) NoorRed else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(NoorBlue),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )

                if (suffix.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suffix,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorTypeSelector(
    selectedType: SensorType?,
    onTypeSelected: (SensorType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SensorType.entries.forEach { type ->
            val isSelected = selectedType == type
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTypeSelected(type) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) type.color.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) type.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(type.icon, null, tint = type.color, modifier = Modifier.size(24.dp))
                        Column {
                            Text(type.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Unité: ${type.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = type.color, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StreetlightDropdown(
    selectedStreetlight: String,
    onStreetlightSelected: (String) -> Unit,
    isError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (isError) NoorRed else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
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
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = NoorAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = selectedStreetlight.ifEmpty { "Sélectionner un lampadaire" },
                        fontSize = 16.sp,
                        color = if (selectedStreetlight.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            mockStreetlights.forEach { streetlight ->
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Lightbulb, null, tint = NoorAmber, modifier = Modifier.size(20.dp))
                            Text(streetlight)
                        }
                    },
                    onClick = {
                        onStreetlightSelected(streetlight)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BatterySlider(
    batteryLevel: Float,
    onBatteryChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    when {
                        batteryLevel > 80 -> Icons.Default.BatteryFull
                        batteryLevel > 50 -> Icons.Default.Battery6Bar
                        batteryLevel > 20 -> Icons.Default.Battery3Bar
                        else -> Icons.Default.Battery1Bar
                    },
                    null,
                    tint = when {
                        batteryLevel > 50 -> NoorGreen
                        batteryLevel > 20 -> NoorAmber
                        else -> NoorRed
                    },
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "${batteryLevel.toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        batteryLevel > 50 -> NoorGreen
                        batteryLevel > 20 -> NoorAmber
                        else -> NoorRed
                    }
                )
            }
            Text(
                when {
                    batteryLevel > 80 -> "Excellent"
                    batteryLevel > 50 -> "Bon"
                    batteryLevel > 20 -> "Faible"
                    else -> "Critique"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = batteryLevel,
            onValueChange = onBatteryChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = when {
                    batteryLevel > 50 -> NoorGreen
                    batteryLevel > 20 -> NoorAmber
                    else -> NoorRed
                },
                activeTrackColor = when {
                    batteryLevel > 50 -> NoorGreen
                    batteryLevel > 20 -> NoorAmber
                    else -> NoorRed
                }
            )
        )
    }
}

@Composable
private fun StatusChips(
    selectedStatus: SensorStatus,
    onStatusSelected: (SensorStatus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SensorStatus.entries.forEach { status ->
            val isSelected = selectedStatus == status
            Surface(
                modifier = Modifier.clickable { onStatusSelected(status) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) status.color.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) status.color
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = status.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        status.displayName,
                        fontSize = 13.sp,
                        color = if (isSelected) status.color else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onCancel: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clickable { onCancel() },
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Annuler", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clickable { onAdd() },
            shape = RoundedCornerShape(12.dp),
            color = NoorBlue
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Ajouter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SuccessAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = NoorGreen,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    "Capteur ajouté !",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Le capteur a été créé avec succès",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Mode Clair")
@Composable
fun AddSensorScreenPreview() {
    SansaTheme(darkTheme = false) {
        AddSensorScreen(onBack = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Mode Sombre")
@Composable
fun AddSensorScreenDarkPreview() {
    SansaTheme(darkTheme = true) {
        AddSensorScreen(onBack = {})
    }
}