package tn.esprit.sansa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.sansa.R
import tn.esprit.sansa.ui.theme.SansaTheme
import tn.esprit.sansa.ui.theme.*

// Palette Noor centralisée
// Définition des pages (images à ajouter dans res/drawable)
data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Bienvenue sur NoorCity",
        description = "La solution intelligente pour la gestion de l'éclairage public et la sécurité urbaine",
        imageRes = R.drawable.welcome_illustration
    ),
    OnboardingPage(
        title = "Surveillez en temps réel",
        description = "Capteurs, caméras, lampadaires et réclamations citoyens — tout au même endroit",
        imageRes = R.drawable.dashboard_preview
    ),
    OnboardingPage(
        title = "Intervenez rapidement",
        description = "Assignation automatique, suivi des techniciens et historique complet",
        imageRes = R.drawable.intervention_map
    ),
    OnboardingPage(
        title = "Prêt à commencer ?",
        description = "Connectez-vous pour accéder à votre tableau de bord",
        imageRes = R.drawable.login_illustration
    )
)

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = onboardingPages[pageIndex]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Image principale
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(320.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = page.title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.description,
                            fontSize = 18.sp,
                            color = Color.Black.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
            }

            // Indicateurs de page (dots)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(onboardingPages.size) { index ->
                    val size by animateDpAsState(
                        targetValue = if (pagerState.currentPage == index) 12.dp else 8.dp,
                        animationSpec = tween(300)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) NoorBlue
                                else Color.LightGray
                            )
                    )
                }
            }

            // Bouton "Se connecter" sur la dernière page
            AnimatedVisibility(
                visible = pagerState.currentPage == onboardingPages.lastIndex,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Se connecter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Bouton Ignorer (Skip)
        TextButton(
            onClick = onLoginClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "Ignorer",
                color = NoorBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    SansaTheme {
        OnboardingScreen(onLoginClick = {})
    }
}