package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.KevSpaceAccent
import com.example.ui.theme.KevSpaceCard
import com.example.ui.theme.KevSpaceDarkBg
import com.example.ui.theme.KevSpaceDeepIndigo
import com.example.ui.theme.KevSpaceOnline
import com.example.ui.theme.KevSpacePrimary
import com.example.ui.theme.KevSpaceSecondary
import com.google.firebase.auth.FirebaseUser

@Composable
fun SidebarScreen(
    currentUser: FirebaseUser,
    users: List<UserProfile>,
    isLoadingUsers: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectUser: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Exclude current user from contact directory
    val otherUsers = remember(users, currentUser.uid) {
        users.filter { it.uid != currentUser.uid }
    }

    val filteredUsers = remember(otherUsers, searchQuery) {
        if (searchQuery.isBlank()) otherUsers
        else otherUsers.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KevSpaceDarkBg)
    ) {
        // KevSpace Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(KevSpaceDeepIndigo, Color(0xFF1E1B4B), Color(0xFF312E81))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(KevSpaceSecondary, KevSpacePrimary)))
                            .border(1.5.dp, KevSpaceAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "KevSpace Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "KevSpace",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Real-Time Cloud Messenger",
                            fontSize = 11.sp,
                            color = KevSpaceSecondary
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(KevSpaceCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Status: Online", color = KevSpaceOnline) },
                            onClick = {
                                onUpdateStatus("Online")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Status: Away", color = Color(0xFFF59E0B)) },
                            onClick = {
                                onUpdateStatus("Away")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Status: Busy", color = Color(0xFFEF4444)) },
                            onClick = {
                                onUpdateStatus("Busy")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign Out", color = Color(0xFFEF4444)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSignOut()
                            }
                        )
                    }
                }
            }
        }

        // Authenticated Profile Card
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = KevSpacePrimary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (currentUser.displayName ?: currentUser.email ?: "U")
                                    .take(1)
                                    .uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(KevSpaceOnline)
                            .border(1.5.dp, Color(0xFF1E293B), CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "KevSpace User",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentUser.email ?: "Authenticated",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign out",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Dynamic Search Bar
        Box(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search active members...", color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = KevSpaceSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.White)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_users_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KevSpacePrimary,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        // Header Label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNITY DIRECTORY (${filteredUsers.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = KevSpaceSecondary,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(KevSpaceOnline)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Live Firestore",
                    fontSize = 11.sp,
                    color = KevSpaceOnline
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic User List
        if (isLoadingUsers) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = KevSpacePrimary,
                    strokeWidth = 2.dp
                )
            }
        } else if (filteredUsers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = KevSpaceSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "No users match '$searchQuery'" else "No other users yet",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "Try searching by another name or email" else "Your user profile is saved in Firestore! When another user signs in with Google, they will appear here in real-time.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("users_list")
            ) {
                items(filteredUsers, key = { it.uid }) { user ->
                    CleanUserListItem(
                        user = user,
                        onClick = { onSelectUser(user) }
                    )
                }
            }
        }
    }
}

@Composable
fun CleanUserListItem(
    user: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag("user_item_${user.uid}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(
                    shape = CircleShape,
                    color = KevSpacePrimary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user.displayName.takeIf { it.isNotBlank() } ?: "U")
                                .take(1)
                                .uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(
                            when (user.status) {
                                "Online" -> KevSpaceOnline
                                "Away" -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            }
                        )
                        .border(1.5.dp, Color(0xFF1E293B), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.displayName.ifBlank { "KevSpace Member" },
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.status,
                        fontSize = 11.sp,
                        color = when (user.status) {
                            "Online" -> KevSpaceOnline
                            "Away" -> Color(0xFFF59E0B)
                            else -> Color(0xFF94A3B8)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.email.ifBlank { "Tap to open chat" },
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
