package com.example.vecrosassignment

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    var rtspUrl by remember { mutableStateOf("rtsp://170.93.143.139/rtplive/470011e600ef003a004ee33696235daa") }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playWhenReady by remember { mutableStateOf(true) }
    var playbackState by remember { mutableStateOf(Player.STATE_IDLE) }
    var playerError by remember { mutableStateOf<PlaybackException?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val newPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    playbackState = state
                }

                override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                    isPlaying = isPlayingValue
                }

                override fun onPlayerError(error: PlaybackException) {
                    playerError = error
                    println("Player Error: ${error.message}")
                }
            })
        }

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(rtspUrl))
            .setMimeType(MimeTypes.APPLICATION_RTSP)
            .build()
        newPlayer.setMediaItem(mediaItem)
        newPlayer.prepare()

        player = newPlayer

        onDispose {
            newPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //video spot
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                player?.let { exoPlayer ->
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                setShutterBackgroundColor(Color.Transparent.toArgb())
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //url spot
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = rtspUrl,
                        onValueChange = { rtspUrl = it },
                        label = { Text("RTSP URL", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    //buttons to play/stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                player?.let { exoPlayer ->
                                    val mediaItem = MediaItem.Builder()
                                        .setUri(Uri.parse(rtspUrl))
                                        .setMimeType(MimeTypes.APPLICATION_RTSP)
                                        .build()
                                    exoPlayer.setMediaItem(mediaItem)
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play", color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                player?.stop()
                                player?.clearMediaItems()
                            },
                            enabled = player != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373),
                                disabledContainerColor = Color(0xFF666666)
                            ),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Stop",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop", color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card
            AnimatedVisibility(
                visible = playerError != null || playbackState != Player.STATE_IDLE,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            playerError != null -> Color(0xFFB71C1C)
                            playbackState == Player.STATE_READY -> Color(0xFF388E3C)
                            playbackState == Player.STATE_BUFFERING -> Color(0xFF1976D2)
                            else -> Color(0xFF2D2D2D)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                playerError != null -> "Error: ${playerError?.message}"
                                else -> "Status: ${playbackStateString(playbackState)}"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun playbackStateString(playbackState: Int): String {
    return when (playbackState) {
        Player.STATE_IDLE -> "Ready to Play"
        Player.STATE_BUFFERING -> "Buffering..."
        Player.STATE_READY -> "Playing"
        Player.STATE_ENDED -> "Playback Ended"
        else -> "Unknown State"
    }
}