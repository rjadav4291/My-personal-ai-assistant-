package com.chintu.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chintu.ai.agent.AutonomousConductor
import com.chintu.ai.ai.provider.GeminiProvider
import com.chintu.ai.core.common.AgentState
import com.chintu.ai.core.common.AppMode
import com.chintu.ai.ui.components.FuturisticOrb
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
        conductor = AutonomousConductor(provider, app.memoryManager, app.toolRegistry, app.selfDevelopmentManager)

        setContent {
            ChintuTheme {
                var appMode by remember { mutableStateOf(AppMode.ONLINE) }
                var agentState by remember { mutableStateOf<AgentState>(AgentState.Ready) }
                var chatLog by remember { mutableStateOf(listOf("CHINTU: સિસ્ટમ અને સેલ્ફ-ડેવલપમેન્ટ એન્જિન તૈયાર છે.")) }
                var textInput by remember { mutableStateOf("") }
                var showKeyDialog by remember { mutableStateOf(false) }
                var apiKeyInput by remember { mutableStateOf(app.secureStorage.getString("GEMINI_API_KEY") ?: "") }
                var gitHubTokenInput by remember { mutableStateOf(app.secureStorage.getString("GITHUB_PAT_TOKEN") ?: "") }

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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHINTU v1.0",
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp
                            )
                            Row {
                                AssistChip(
                                    onClick = {
                                        appMode = when (appMode) {
                                            AppMode.ONLINE -> AppMode.OFFLINE
                                            AppMode.OFFLINE -> AppMode.PRIVATE
                                            AppMode.PRIVATE -> AppMode.AUTO
                                            AppMode.AUTO -> AppMode.ONLINE
                                        }
                                    },
                                    label = { Text(appMode.name) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { showKeyDialog = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            FuturisticOrb(state = agentState)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            items(chatLog) { msg ->
                                Text(
                                    text = msg,
                                    color = if (msg.startsWith("User:")) Color.White else Color(0xFF00E5FF),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("આદેશ આપો...") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
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
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (showKeyDialog) {
                    AlertDialog(
                        onDismissRequest = { showKeyDialog = false },
                        title = { Text("AI & GitHub Configuration") },
                        text = {
                            Column {
                                Text("Gemini API Key:", fontSize = 12.sp)
                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = { apiKeyInput = it },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(12.dp))
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
                                Text("Cancel")
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
