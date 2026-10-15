package com.chintu.ai

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.agent.AutonomousConductor
import com.chintu.ai.ai.provider.GeminiProvider
import com.chintu.ai.core.common.AgentState
import com.chintu.ai.core.common.AppMode
import com.chintu.ai.ui.components.AgentPanel
import com.chintu.ai.ui.components.FuturisticOrb
import com.chintu.ai.ui.components.cyberBorder
import com.chintu.ai.ui.theme.ChintuTheme
import com.chintu.ai.voice.SpeechRecognizerManager
import com.chintu.ai.voice.TextToSpeechManager
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var speechManager: SpeechRecognizerManager
    private lateinit var conductor: AutonomousConductor

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ChintuApplication
        ttsManager = TextToSpeechManager(this)
        val provider = GeminiProvider(app.secureStorage)
        conductor = AutonomousConductor(
            provider = provider,
            memory = app.memoryManager,
            tools = app.toolRegistry,
            selfDevManager = app.selfDevelopmentManager,
            skillManager = app.skillManager
        )

        setContent {
            ChintuTheme {
                var appMode by remember { mutableStateOf(AppMode.ONLINE) }
                var agentState by remember { mutableStateOf<AgentState>(AgentState.Ready) }
                var chatLog by remember { mutableStateOf(listOf("CHINTU: સિસ્ટમ, પ્રોડક્ટિવિટી અને પ્રાઇવસી કંટ્રોલ સક્રિય છે.", "User: તમારા એજન્ટ્સ બતાવો.", "CHINTU: બિલકુલ, આ મારા સક્રિય એજન્ટ્સ અને તેમની લાઇવ સ્થિતિ છે:")) }
                var textInput by remember { mutableStateOf("") }
                var showKeyDialog by remember { mutableStateOf(false) }
                var apiKeyInput by remember { mutableStateOf(app.secureStorage.getString("GEMINI_API_KEY") ?: "") }
                var gitHubTokenInput by remember { mutableStateOf(app.secureStorage.getString("GITHUB_PAT_TOKEN") ?: "") }

                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        agentState = AgentState.Thinking("ફોટો એનાલાઈઝ કરી રહ્યો છું...")
                        chatLog = chatLog + "User: [ઇમેજ અપલોડ કરી]"
                        lifecycleScope.launch {
                            val visionResult = app.visionEngine.analyzeImage(it)
                            val reply = visionResult.fold(
                                onSuccess = { text -> text },
                                onFailure = { e -> "વિઝન પ્રોસેસિંગ નિષ્ફળ: ${e.localizedMessage}" }
                            )
                            chatLog = chatLog + "CHINTU: $reply"
                            agentState = AgentState.Speaking(reply)
                            ttsManager.speak(reply) {
                                agentState = AgentState.Ready
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    speechManager = SpeechRecognizerManager(
                        context = this@MainActivity,
                        onResult = { recognizedText ->
                            agentState = AgentState.Thinking("સમજી રહ્યો છું...")
                            chatLog = chatLog + "User: $recognizedText"
                            lifecycleScope.launch {
                                val reply = conductor.processQuery(recognizedText, appMode) { state ->
                                    agentState = state
                                }
                                chatLog = chatLog + "CHINTU: $reply"
                                agentState = AgentState.Speaking(reply)
                                ttsManager.speak(reply) {
                                    agentState = AgentState.Ready
                                }
                            }
                        },
                        onError = { error ->
                            agentState = AgentState.Error(error)
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0A0E14))
                    ) {
                        // Left Sidebar - Upload controls
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(130.dp)
                                .background(Color(0xFF0E131F))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cyberBorder(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101622))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                        Icon(Icons.Default.Image, contentDescription = "Upload", tint = Color(0xFF00E5FF))
                                    }
                                    Text("Image/Upload", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Dynamic Visual sound wave/Indicator
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(25.dp))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(25.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎙️", fontSize = 18.sp)
                            }
                        }

                        // Middle Section - Main Conversation & HUD Orb
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(12.dp)
                        ) {
                            // Top Bar inside main panel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHINTU v1.0",
                                    color = Color(0xFF00E5FF),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = {
                                            appMode = when (appMode) {
                                                AppMode.ONLINE -> AppMode.OFFLINE
                                                AppMode.OFFLINE -> AppMode.PRIVATE
                                                AppMode.PRIVATE -> AppMode.AUTO
                                                AppMode.AUTO -> AppMode.ONLINE
                                            }
                                        },
                                        label = { Text(appMode.name, color = Color(0xFF00E5FF)) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF101622))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { showKeyDialog = true }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                                    }
                                }
                            }

                            // Panoramic layout with centered AI Orb
                            Box(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                FuturisticOrb(state = agentState)
                            }

                            // Cinematic Transparent Chat Screen
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .fillMaxWidth()
                                    .cyberBorder()
                                    .background(Color(0xFF0E131F).copy(alpha = 0.85f))
                                    .padding(10.dp)
                            ) {
                                items(chatLog) { msg ->
                                    Text(
                                        text = msg,
                                        color = if (msg.startsWith("User:")) Color.White else Color(0xFF00E5FF),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }

                            // Active Search/Action Input Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .cyberBorder()
                                    .background(Color(0xFF101622))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("આદેશ આપો...", color = Color.Gray, fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                IconButton(onClick = {
                                    if (ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        agentState = AgentState.Listening
                                        speechManager.startListening(Locale("gu", "IN"))
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = Color(0xFF00E5FF))
                                }
                                IconButton(onClick = {
                                    if (textInput.isNotBlank()) {
                                        val input = textInput
                                        textInput = ""
                                        chatLog = chatLog + "User: $input"
                                        lifecycleScope.launch {
                                            val reply = conductor.processQuery(input, appMode) { state ->
                                                agentState = state
                                            }
                                            chatLog = chatLog + "CHINTU: $reply"
                                            ttsManager.speak(reply)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00E5FF))
                                }
                            }
                        }

                        // Right Sidebar - Active Agents
                        AgentPanel()
                    }
                }

                if (showKeyDialog) {
                    AlertDialog(
                        onDismissRequest = { showKeyDialog = false },
                        title = { Text("Settings & Privacy Center") },
                        text = {
                            Column {
                                Text("Gemini API Key:", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = { apiKeyInput = it },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("GitHub Token (PAT for Self-Upgrade):", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = gitHubTokenInput,
                                    onValueChange = { gitHubTokenInput = it },
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                app.secureStorage.saveString("GEMINI_API_KEY", apiKeyInput.trim())
                                app.secureStorage.saveString("GITHUB_PAT_TOKEN", gitHubTokenInput.trim())
                                showKeyDialog = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showKeyDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        speechManager.stopListening()
    }
}
