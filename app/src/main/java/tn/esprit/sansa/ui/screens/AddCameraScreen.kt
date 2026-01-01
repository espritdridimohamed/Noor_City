// AddCameraScreen.kt — Écran d'ajout de caméra avec design Noor premium REDESIGN
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
import tn.esprit.sansa.ui.theme.*

import androidx.compose.ui.text.style.TextAlign
import tn.esprit.sansa.ui.screens.models.*
import tn.esprit.sansa.ui.viewmodels.CamerasViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// Palette Noor centralisée dans tn.esprit.sansa.ui.theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraScreen(
    onAddSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CamerasViewModel = viewModel()
) {
    var location by remember { mutableStateOf("") }
    var associatedStreetlight by remember { mutableStateOf("") }
    var type by remember { mutableStateOf<CameraType?>(null) }
    var status by remember { mutableStateOf(CameraStatus.ONLINE) }
    var resolution by remember { mutableStateOf("1080p") }
    var zone by remember { mutableStateOf("") }
    var nightVision by remember { mutableStateOf(true) }
    var recordingEnabled by remember { mutableStateOf(true) }
    var motionDetection by remember { mutableStateOf(true) }
    var streamUrl by remember { mutableStateOf("") }

    var showLocationError by remember { mutableStateOf(false) }
    var showStreetlightError by remember { mutableStateOf(false) }
    var showTypeError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showSuccessAnimation by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nouvelle caméra", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Sécurité et surveillance intelligente", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onAddSuccess) {
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
                // Section 1: Type
                FormSection(
                    title = "Type de caméra",
                    icon = Icons.Default.Videocam,
                    isError = showTypeError
                ) {
                    CameraTypeSelector(
                        selectedType = type,
                        onTypeSelected = {
                            type = it
                            showTypeError = false
                        }
                    )
                }

                // Section 2: Localisation
                FormSection(
                    title = "Emplacement & Zone",
                    icon = Icons.Default.LocationOn,
                    isError = showLocationError || showStreetlightError
                ) {
                    CustomTextField(
                        value = location,
                        onValueChange = { location = it; showLocationError = false },
                        label = "Localisation",
                        placeholder = "Ex: Entrée Nord, Parking B...",
                        isError = showLocationError
                    )
                    
                    Spacer(Modifier.height(12.dp))

                    CustomTextField(
                        value = associatedStreetlight,
                        onValueChange = { associatedStreetlight = it; showStreetlightError = false },
                        label = "ID Lampadaire",
                        placeholder = "Ex: L001",
                        isError = showStreetlightError
                    )

                    Spacer(Modifier.height(12.dp))

                    CustomTextField(
                        value = zone,
                        onValueChange = { zone = it },
                        label = "Zone de la ville",
                        placeholder = "Ex: Centre-ville, Zone Industrielle..."
                    )

                    Spacer(Modifier.height(12.dp))

                    CustomTextField(
                        value = streamUrl,
                        onValueChange = { streamUrl = it },
                        label = "Adresse IP / Stream URL",
                        placeholder = "Ex: http://192.168.1.50:81/stream"
                    )
                }

                // Section 3: Paramètres Techniques
                FormSection(
                    title = "Paramètres techniques",
                    icon = Icons.Default.Settings
                ) {
                    Text("Résolution", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ResolutionChips(
                        selectedResolution = resolution,
                        onResolutionSelected = { resolution = it }
                    )
                    
                    Spacer(Modifier.height(12.dp))

                    Text("Options de surveillance", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ToggleOptions(
                        nightVision = nightVision,
                        onNightVisionChange = { nightVision = it },
                        recordingEnabled = recordingEnabled,
                        onRecordingChange = { recordingEnabled = it },
                        motionDetection = motionDetection,
                        onMotionChange = { motionDetection = it }
                    )
                }

                // Section 4: Statut Initial
                FormSection(
                    title = "Statut initial",
                    icon = Icons.Default.CheckCircle
                ) {
                    CameraStatusChips(
                        selectedStatus = status,
                        onStatusSelected = { status = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Boutons d'action
                ActionButtons(
                    onCancel = onAddSuccess,
                    onAdd = {
                        var hasError = false
                        if (type == null) { showTypeError = true; hasError = true }
                        if (location.isBlank()) { showLocationError = true; hasError = true }
                        if (associatedStreetlight.isBlank()) { showStreetlightError = true; hasError = true }

                        if (!hasError) {
                            val newCamera = Camera(
                                id = "CAM_${UUID.randomUUID().toString().take(6).uppercase()}",
                                location = location,
                                status = status,
                                associatedStreetlight = associatedStreetlight,
                                type = type!!,
                                resolution = resolution,
                                zone = zone,
                                nightVision = nightVision,
                                recordingEnabled = recordingEnabled,
                                motionDetection = motionDetection,
                                installDate = "30/12/2025",
                                lastMaintenance = "Jamais",
                                streamUrl = streamUrl
                            )
                            viewModel.addCamera(newCamera) { success ->
                                if (success) {
                                    showSuccessAnimation = true
                                    scope.launch {
                                        delay(1200)
                                        onAddSuccess()
                                    }
                                } else {
                                    Toast.makeText(context, "Erreur lors de l'ajout à la base de données", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut()
            ) {
                CameraSuccessAnimation()
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isError) BorderStroke(2.dp, NoorRed) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isError) NoorRed.copy(alpha = 0.1f) else NoorBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = if (isError) NoorRed else NoorBlue, modifier = Modifier.size(20.dp))
                }
                Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 13.sp, color = if (isError) NoorRed else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.5.dp,
                    color = if (isError) NoorRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(NoorBlue),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(text = placeholder, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun CameraTypeSelector(
    selectedType: CameraType?,
    onTypeSelected: (CameraType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CameraType.entries.forEach { t ->
            val isSelected = selectedType == t
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTypeSelected(t) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) t.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) t.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(t.color.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(t.icon, null, tint = t.color, modifier = Modifier.size(24.dp))
                    }
                    Text(t.displayName, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = t.color, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionChips(
    selectedResolution: String,
    onResolutionSelected: (String) -> Unit
) {
    val resolutions = listOf("720p", "1080p", "2K", "4K", "8K")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        resolutions.forEach { res ->
            val isSelected = selectedResolution == res
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onResolutionSelected(res) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) NoorBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Text(
                    text = res,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CameraStatusChips(
    selectedStatus: CameraStatus,
    onStatusSelected: (CameraStatus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CameraStatus.entries.forEach { s ->
            val isSelected = selectedStatus == s
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStatusSelected(s) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) s.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) s.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = s.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) s.color else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleOptions(
    nightVision: Boolean, onNightVisionChange: (Boolean) -> Unit,
    recordingEnabled: Boolean, onRecordingChange: (Boolean) -> Unit,
    motionDetection: Boolean, onMotionChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleItem(icon = Icons.Default.Nightlight, label = "Vision Nocturne", checked = nightVision, onCheckedChange = onNightVisionChange)
        ToggleItem(icon = Icons.Default.FiberManualRecord, label = "Enregistrement", checked = recordingEnabled, onCheckedChange = onRecordingChange)
        ToggleItem(icon = Icons.Default.TransferWithinAStation, label = "Détection Mouvement", checked = motionDetection, onCheckedChange = onMotionChange)
    }
}

@Composable
private fun ToggleItem(icon: ImageVector, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = if (checked) NoorBlue else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Text(label, fontSize = 14.sp)
            }
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = NoorBlue, checkedTrackColor = NoorBlue.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun ActionButtons(onCancel: () -> Unit, onAdd: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("Annuler", fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ajouter", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CameraSuccessAnimation() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = NoorGreen, modifier = Modifier.size(80.dp))
                Text("Caméra Ajoutée !", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Votre dispositif est prêt et configuré.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddCameraScreenPreview() {
    SansaTheme { AddCameraScreen(onAddSuccess = {}) }
}