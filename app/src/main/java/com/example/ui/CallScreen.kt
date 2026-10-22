package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveCall
import com.example.model.CallState
import com.example.model.CallType
import com.example.ui.theme.KevSpaceAccent
import com.example.ui.theme.KevSpaceDarkBg
import com.example.ui.theme.KevSpaceDeepIndigo
import com.example.ui.theme.KevSpaceOnline
import com.example.ui.theme.KevSpacePrimary
import com.example.ui.theme.KevSpaceSecondary

@Composable
fun CallScreen(
    call: ActiveCall,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCameraFlip: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onEndCall)

    val durationText = String.format(
        "%02d:%02d",
        call.durationSeconds / 60,
        call.durationSeconds % 60
    )

    // Pulsating animation for ringing and active call audio waves
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        KevSpaceDeepIndigo,
                        KevSpaceDarkBg
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Main Calling Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = KevSpacePrimary.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KevSpaceSecondary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (call.callType == CallType.VIDEO) "KevSpace Video Call" else "KevSpace Audio Call",
                        color = KevSpaceSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = call.recipient.displayName.ifBlank { "KevSpace Member" },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                val stateLabel = when (call.state) {
                    CallState.CONNECTING -> "Connecting to KevSpace relay..."
                    CallState.RINGING -> "Ringing..."
                    CallState.CONNECTED -> "Connected • $durationText"
                    CallState.ENDED -> "Call Ended"
                }

                Text(
                    text = stateLabel,
                    fontSize = 15.sp,
                    color = if (call.state == CallState.CONNECTED) KevSpaceOnline else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            // Center Viewport: Video Preview or Audio Wave Avatar
            if (call.callType == CallType.VIDEO) {
                // Video Screen Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, KevSpacePrimary.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated Remote Video Feed
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        KevSpaceDeepIndigo,
                                        Color(0xFF090D16)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = KevSpaceSecondary,
                                modifier = Modifier.size(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = call.recipient.displayName.take(1).uppercase(),
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Live HD Video Feed",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Local Camera Picture-in-Picture (PiP)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(width = 110.dp, height = 150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, KevSpaceAccent, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (call.isCameraOff) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = "Camera off",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Camera Off",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                KevSpacePrimary.copy(alpha = 0.8f),
                                                KevSpaceDeepIndigo
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (call.isFrontCamera) "You (Front)" else "You (Rear)",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                // Audio Wave Visualizer Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Pulsating Ring
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(KevSpacePrimary.copy(alpha = 0.12f))
                    )

                    // Inner Pulsating Ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(if (call.state == CallState.CONNECTED) pulseScale * 0.95f else 1f)
                            .clip(CircleShape)
                            .background(KevSpaceSecondary.copy(alpha = 0.20f))
                    )

                    // Recipient Planetary Avatar
                    Surface(
                        shape = CircleShape,
                        color = KevSpacePrimary,
                        modifier = Modifier
                            .size(108.dp)
                            .border(3.dp, KevSpaceAccent, CircleShape),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = call.recipient.displayName.take(1).uppercase(),
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Controls Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Mic Toggle
                    CallControlButton(
                        icon = if (call.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (call.isMicMuted) "Unmute" else "Mute",
                        isActive = call.isMicMuted,
                        activeColor = Color(0xFFEF4444),
                        onClick = onToggleMic,
                        testTag = "call_toggle_mic"
                    )

                    // 2. Camera Toggle (for video calls)
                    if (call.callType == CallType.VIDEO) {
                        CallControlButton(
                            icon = if (call.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            label = if (call.isCameraOff) "Turn On" else "Camera",
                            isActive = call.isCameraOff,
                            activeColor = Color(0xFFEF4444),
                            onClick = onToggleCamera,
                            testTag = "call_toggle_camera"
                        )

                        // 3. Flip Camera
                        CallControlButton(
                            icon = Icons.Default.Cameraswitch,
                            label = "Flip",
                            isActive = false,
                            onClick = onToggleCameraFlip,
                            testTag = "call_flip_camera"
                        )
                    }

                    // 4. Speaker Toggle
                    CallControlButton(
                        icon = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = if (call.isSpeakerOn) "Speaker" else "Earpiece",
                        isActive = call.isSpeakerOn,
                        activeColor = KevSpaceSecondary,
                        onClick = onToggleSpeaker,
                        testTag = "call_toggle_speaker"
                    )

                    // 5. End Call Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onEndCall,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("call_end_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End call",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = KevSpaceSecondary,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) activeColor else Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive) activeColor else Color(0xFF334155)
            ),
            modifier = Modifier.size(52.dp)
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(testTag)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive && activeColor == KevSpaceSecondary) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }
}
