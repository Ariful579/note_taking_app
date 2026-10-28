package com.notewise.tabnotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notewise.tabnotes.ui.theme.NotewiseBlue

enum class TabletNavDestination {
    EDITOR,
    LIBRARY,
    FAVORITES
}

@Composable
fun TabletNavigationRail(
    currentDestination: TabletNavDestination,
    onNavigate: (TabletNavDestination) -> Unit,
    onNewNotebook: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            // App Icon / Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = NotewiseBlue,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Notewise Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationRailItem(
                    selected = currentDestination == TabletNavDestination.EDITOR,
                    onClick = { onNavigate(TabletNavDestination.EDITOR) },
                    icon = { Icon(Icons.Default.Draw, contentDescription = "Note Editor") },
                    label = { Text("Note", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = currentDestination == TabletNavDestination.LIBRARY,
                    onClick = { onNavigate(TabletNavDestination.LIBRARY) },
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Notebooks") },
                    label = { Text("Library", fontSize = 10.sp) }
                )

                NavigationRailItem(
                    selected = currentDestination == TabletNavDestination.FAVORITES,
                    onClick = { onNavigate(TabletNavDestination.FAVORITES) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                    label = { Text("Starred", fontSize = 10.sp) }
                )
            }

            // Bottom Add Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = onNewNotebook,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Notebook")
                }
            }
        }
    }
}
