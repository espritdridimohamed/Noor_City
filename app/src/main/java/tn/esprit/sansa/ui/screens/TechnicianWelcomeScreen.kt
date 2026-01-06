package tn.esprit.sansa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.navigation.Screen
import tn.esprit.sansa.ui.theme.NoorBlue
import tn.esprit.sansa.ui.theme.NoorIndigo
import tn.esprit.sansa.ui.viewmodels.AuthViewModel
import tn.esprit.sansa.ui.viewmodels.AuthState

data class TechFeature(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianWelcomeScreen(
    onNavigate: (String) -> Unit,
    onOnboardingComplete: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val features = listOf(
        TechFeature("Lampadaires", Icons.Default.Lightbulb, Screen.Streetlights.route, NoorBlue),
        TechFeature("Caméras", Icons.Default.Videocam, Screen.Cameras.route, Color(0xFF00BCD4)),
        TechFeature("Interventions", Icons.Default.Construction, Screen.Interventions.route, Color(0xFFFF9800)),
        TechFeature("Capteurs", Icons.Default.Sensors, Screen.Sensors.route, Color(0xFF4CAF50))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NoorBlue.copy(alpha = 0.05f), Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            
            // Header Section
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = NoorBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Engineering, null, tint = NoorBlue, modifier = Modifier.size(40.dp))
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Bienvenue, ${currentUser?.name ?: "Technicien"}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Votre espace de travail intelligent est prêt. Accédez rapidement à vos outils.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            
            Spacer(Modifier.height(40.dp))
            
            // Quick Access Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(features) { feature ->
                    FeatureCard(feature = feature, onClick = { onNavigate(feature.route) })
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Finalization Button
            Button(
                onClick = {
                    authViewModel.activateTechnicianAccount()
                    onOnboardingComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoorBlue)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Commencer maintenant", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Vous pourrez modifier votre zone et numéro plus tard dans votre profil.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeatureCard(feature: TechFeature, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(feature.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, null, tint = feature.color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                feature.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
