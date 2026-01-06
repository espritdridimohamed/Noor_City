package tn.esprit.sansa.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeActionsContainer(
    item: T,
    onDelete: (T) -> Unit,
    onEdit: ((T) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (onEdit != null) {
                        onEdit(item)
                    }
                    false
                }
                else -> false
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cet élément ? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(item)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = onEdit != null,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFFD600) // Deep Yellow
                    else -> Color.Transparent
                },
                label = "backgroundColor"
            )
            
            val scale by animateFloatAsState(
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f,
                label = "iconScale"
            )

            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            val icon = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                else -> Icons.Default.Delete
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Color.Black else Color.White,
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        },
        content = {
            content(item)
        }
    )
}
