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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.chintu.ai.memory.MemoryManager
import com.chintu.ai.ui.components.JarvisHudCore
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
    private lateinit var memoryManager: MemoryManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ChintuApplication
        ttsManager = TextToSpeechManager(this)
        memoryManager = app.memoryManager
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
                var selectedTab by remember { mutableStateOf(0) }
                var appMode by remember { mutableStateOf(AppMode.ONLINE) }
                var agentState by remember { mutableStateOf<AgentState>(AgentState.Ready) }
                
                val savedHistory = remember { memoryManager.loadConversation() }
                var chatLog by remember {
                    mutableStateOf(
                        if (savedHistory.isNotEmpty()) savedHistory
                        else listOf("CHINTU: JARVIS HUD ઓનલાઇન. સિસ્ટમ તૈયાર છે.")
                    )
                }
                var textInput by remember { mutableStateOf("") }
                var upgradeTaskInput by remember { mutableStateOf("") }
                var upgradeLog by remember { mutableStateOf<List<String>>(listOf("સેલ્ફ-અપગ્રેડ સિસ્ટમ સક્રિય છે.")) }
                var showKeyDialog by remember { mutableStateOf(false) }

                var apiKeyInput by remember {
                    mutableStateOf(app.secureStorage.getString("API_KEY") ?: app.secureStorage.getString("GEMINI_API_KEY") ?: "")
                }
                var serverUrlInput by remember {
                    mutableStateOf(app.secureStorage.getString("SERVER_ENDPOINT") ?: "")
                }
                var modelNameInput by remember {
                    mutableStateOf(app.secureStorage.getString("MODEL_NAME") ?: "google/gemini-flash-1.5")
                }
                var gitHubTokenInput by remember {
                    mutableStateOf(app.secureStorage.getString("GITHUB_PAT_TOKEN") ?: "")
                }

                fun appendMessage(msg: String) {
                    val updated = chatLog + msg
                    chatLog = updated
                    memoryManager.saveConversation(updated)
                }

                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        agentState = AgentState.Thinking("ફોટો એનાલાઈઝ કરી રહ્યો છું...")
                        appendMessage("User: [ઇમેજ વિઝન સ્કેન]")
                        lifecycleScope.launch {
                            val visionResult = app.visionEngine.analyzeImage(it)
                            val reply = visionResult.fold(
                                onSuccess = { text -> text },
                                onFailure = { e -> "વિઝન પ્રોસેસિંગ નિષ્ફળ: ${e.localizedMessage}" }
                            )
                            appendMessage("CHINTU: $reply")
                            agentState = AgentState.Speaking(reply)
                            ttsManager.speak(reply) { agentState = AgentState.Ready }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    speechManager = SpeechRecognizerManager(
                        context = this@MainActivity,
                        onResult = { recognizedText ->
                            agentState = AgentState.Thinking("સમજી રહ્યો છું...")
                            appendMessage("User: $recognizedText")
                            lifecycleScope.launch {
                                val reply = conductor.processQuery(recognizedText, appMode) { state ->
                                    agentState = state
                                }
                                appendMessage("CHINTU: $reply")
                                agentState = AgentState.Speaking(reply)
                                ttsManager.speak(reply) { agentState = AgentState.Ready }
                            }
                        },
                        onError = { error -> agentState = AgentState.Error(error) }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF070B12))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHINTU v1.0",
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    modifier = Modifier.width(180.dp),
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF00E5FF)
                                ) {
                                    Tab(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        text = { Text("HUD", fontSize = 12.sp) }
                                    )
                                    Tab(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        text = { Text("UPGRADE", fontSize = 12.sp) }
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(onClick = { showKeyDialog = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                                }
                            }
                        }

                        if (selectedTab == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                JarvisHudCore(state = agentState)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    "ORCH" to Color(0xFF00FF66),
                                    "RESEARCH" to Color(0xFF00E5FF),
                                    "CODER" to Color(0xFFFF9100),
                                    "VOICE" to Color(0xFFB388FF)
                                ).forEach { (name, color) ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF101622), RoundedCornerShape(6.dp))
                                            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("● $name", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .cyberBorder()
                                    .background(Color(0xFF101622))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                    Icon(Icons.Default.Image, contentDescription = "Vision", tint = Color(0xFF00E5FF))
                                }
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
                                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        agentState = AgentState.Listening
                                        speechManager.startListening(Locale("gu", "IN"))
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice", tint = Color(0xFF00E5FF))
                                }
                                IconButton(onClick = {
                                    if (textInput.isNotBlank()) {
                                        val input = textInput
                                        textInput = ""
                                        appendMessage("User: $input")
                                        lifecycleScope.launch {
                                            val reply = conductor.processQuery(input, appMode) { state -> agentState = state }
                                            appendMessage("CHINTU: $reply")
                                            ttsManager.speak(reply)
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00E5FF))
                                }
                            }
                        }
                                          // PAGE 2: Self-Upgrade & GitHub Evolution Hub
                        if (selectedTab == 1) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "⚡ AUTONOMOUS EVOLUTION HUB",
                                    color = Color(0xFF00FF66),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "અહીંથી તમે સીધો કોડ જનરેટ કરાવીને GitHub repo માં અપગ્રેડ પુશ કરી શકો છો.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                OutlinedTextField(
                                    value = upgradeTaskInput,
                                    onValueChange = { upgradeTaskInput = it },
                                    label = { Text("કયું નવું ફીચર ઉમેરવું છે?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (upgradeTaskInput.isNotBlank()) {
                                            val task = upgradeTaskInput
                                            upgradeTaskInput = ""
                                            upgradeLog = upgradeLog + "શરૂ કર્યું: $task..."
                                            lifecycleScope.launch {
                                                val res = app.selfDevelopmentManager.developAndDeployFeature(task, "Direct Upgrade Trigger")
                                                res.fold(
                                                    onSuccess = { prUrl ->
                                                        upgradeLog = upgradeLog + "સફળતાપૂર્વક કમિટ થયું! 🚀 લિંક: $prUrl"
                                                    },
                                                    onFailure = { err ->
                                                        upgradeLog = upgradeLog + "નિષ્ફળ: ${err.localizedMessage}"
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                                ) {
                                    Text("GitHub માં ઓટો-અપગ્રેડ પુશ કરો", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Text("Evolution Logs:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .cyberBorder()
                                        .background(Color(0xFF0E131F))
                                        .padding(8.dp)
                                ) {
                                    items(upgradeLog) { log ->
                                        Text(log, color = Color(0xFF00FF66), fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (showKeyDialog) {
                    AlertDialog(
                        onDismissRequest = { showKeyDialog = false },
                        title = { Text("Settings & Privacy Center") },
                        text = {
                            Column {
                                Text("API Key (OpenRouter / Gemini):", fontSize = 11.sp)
                                OutlinedTextField(value = apiKeyInput, onValueChange = { apiKeyInput = it }, singleLine = true)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Model Name:", fontSize = 11.sp)
                                OutlinedTextField(value = modelNameInput, onValueChange = { modelNameInput = it }, singleLine = true)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Custom Server Endpoint:", fontSize = 11.sp)
                                OutlinedTextField(value = serverUrlInput, onValueChange = { serverUrlInput = it }, singleLine = true)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("GitHub Token (PAT):", fontSize = 11.sp)
                                OutlinedTextField(value = gitHubTokenInput, onValueChange = { gitHubTokenInput = it }, singleLine = true)
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                app.secureStorage.saveString("API_KEY", apiKeyInput.trim())
                                app.secureStorage.saveString("MODEL_NAME", modelNameInput.trim())
                                app.secureStorage.saveString("SERVER_ENDPOINT", serverUrlInput.trim())
                                app.secureStorage.saveString("GITHUB_PAT_TOKEN", gitHubTokenInput.trim())
                                showKeyDialog = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showKeyDialog = false }) { Text("Close") }
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
