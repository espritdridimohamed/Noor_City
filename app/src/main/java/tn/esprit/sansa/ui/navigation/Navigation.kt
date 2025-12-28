package tn.esprit.sansa.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tn.esprit.sansa.ui.screens.*

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Streetlights     : Screen("streetlights", "Lampadaires", Icons.Default.LightMode)
    object Cameras          : Screen("cameras", "Caméras", Icons.Default.Videocam)
    object Technicians      : Screen("technicians", "Techniciens", Icons.Default.Engineering)
    object Citizens         : Screen("citizens", "Citoyens", Icons.Default.People)
    object Reclamations     : Screen("reclamations", "Réclamations", Icons.Default.ReportProblem)

    object Zones            : Screen("zones", "Zones", Icons.Default.Map)
    object Interventions    : Screen("interventions", "Interventions", Icons.Default.Construction)
    object CulturalEvents   : Screen("cultural_events", "Év. culturels", Icons.Default.Event)
    object LightingPrograms : Screen("lighting_programs", "Programmes", Icons.Default.AutoMode)
    object Sensors          : Screen("sensors", "Capteurs", Icons.Default.Sensors)

    object AddStreetlight     : Screen("add_streetlight", "", Icons.Default.AddCircle)
    object AddCamera          : Screen("add_camera", "", Icons.Default.AddCircle)
    object AddTechnician      : Screen("add_technician", "", Icons.Default.AddCircle)
    object AddCitizen         : Screen("add_citizen", "", Icons.Default.AddCircle)
    object AddReclamation     : Screen("add_reclamation", "", Icons.Default.AddCircle)
    object AddIntervention    : Screen("add_intervention", "", Icons.Default.AddCircle)
    object AddCulturalEvent   : Screen("add_cultural_event", "", Icons.Default.AddCircle)
    object AddLightingProgram : Screen("add_lighting_program", "", Icons.Default.AddCircle)
    object AddZone            : Screen("add_zone", "", Icons.Default.AddCircle)
    object AddSensor          : Screen("add_sensor", "", Icons.Default.AddCircle)

    object Home    : Screen("home", "Accueil", Icons.Default.Home)
    object History : Screen("history", "Historique", Icons.Default.History)
    object Settings: Screen("settings", "Paramètres", Icons.Default.Settings)
}

@Composable
fun ModernBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val primaryItems = listOf(
        Screen.Streetlights,
        Screen.Cameras,
        Screen.Technicians,
        Screen.Citizens,
        Screen.Reclamations
    )

    var showSecondarySheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(
                width = 0.6.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        primaryItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(if (selected) 30.dp else 26.dp),
                        tint = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    indicatorColor = Color.Transparent
                )
            )
        }

        NavigationBarItem(
            selected = false,
            onClick = { showSecondarySheet = true },
            icon = {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Autres fonctionnalités",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = null,
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                indicatorColor = Color.Transparent
            )
        )
    }

    if (showSecondarySheet) {
        SecondaryFeaturesBottomSheet(
            onDismiss = { showSecondarySheet = false },
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryFeaturesBottomSheet(
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    val secondaryItems = listOf(
        Screen.Zones,
        Screen.Interventions,
        Screen.CulturalEvents,
        Screen.LightingPrograms,
        Screen.Sensors
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Autres fonctionnalités",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(secondaryItems) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                onDismiss()
                            }
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AppNavigationWithModernBar(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            ModernBottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Technicians.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Streetlights.route) { StreetlightsMapScreen() }

            composable(Screen.Cameras.route) {
                CamerasScreen(
                    onNavigateToAddCamera = {
                        navController.navigate(Screen.AddCamera.route)
                    }
                )
            }

            composable(Screen.Technicians.route) { TechniciansScreen() }
            composable(Screen.Citizens.route) { CitizensScreen() }
            composable(Screen.Reclamations.route) { ReclamationsScreen() }

            composable(Screen.Zones.route) { ZonesScreen() }
            composable(Screen.Interventions.route) { InterventionsScreen() }
            composable(Screen.CulturalEvents.route) { CulturalEventsScreen() }

            composable(Screen.LightingPrograms.route) {
                LightingProgramsScreen(
                    onNavigateToAddProgram = {
                        navController.navigate(Screen.AddLightingProgram.route)
                    }
                )
            }

            composable(Screen.Sensors.route) { SensorsScreenModern() }

            composable(Screen.AddCamera.route) {
                AddCameraScreen(
                    onAddSuccess = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(Screen.AddLightingProgram.route) {
                AddLightingProgramScreen(
                    onAddSuccess = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(Screen.AddStreetlight.route) {
                Text("Ajout lampadaire - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddTechnician.route) {
                Text("Ajout technicien - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddCitizen.route) {
                Text("Ajout citoyen - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddReclamation.route) {
                Text("Ajout réclamation - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddIntervention.route) {
                Text("Ajout intervention - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddCulturalEvent.route) {
                Text("Ajout événement culturel - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddZone.route) {
                Text("Ajout zone - À implémenter", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
            composable(Screen.AddSensor.route) {
                AddSensorScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Home.route) { HomeScreenModern() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}