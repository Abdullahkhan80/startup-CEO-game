package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Production-Ready Crash Reporting and Event Telemetry
        CrashReporter.initialize(applicationContext)
        AnalyticsTracker.initialize(applicationContext)
        AnalyticsTracker.logEvent("app_launch", mapOf("timestamp" to System.currentTimeMillis()))
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgSlate,
                    floatingActionButton = {
                        val state by viewModel.uiState.collectAsState()
                        if (!state.isGameOver && !state.isIPOExited && state.activeDecisionId == null) {
                            ExtendedFloatingActionButton(
                                text = { Text("NEXT WEEK", fontWeight = FontWeight.Black) },
                                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week") },
                                onClick = { viewModel.advanceWeek() },
                                containerColor = Indigo500,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("next_week_fab")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .drawBehind {
                                // Draw a rich deep radial glow behind the glass cards
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Indigo500.copy(alpha = 0.15f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                                        radius = size.width
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Amber400.copy(alpha = 0.08f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                                        radius = size.width * 1.2f
                                    )
                                )
                            }
                    ) {
                        GameDashboard(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun GameDashboard(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val candidates by viewModel.candidates.collectAsState()
    val fundingOffers by viewModel.fundingOffers.collectAsState()

    var selectedTab by remember { mutableStateOf("Office") } // "Office", "Market", "Analytics", "Exit"
    var showOnboarding by remember { mutableStateOf(false) }
    var showHiringPortal by remember { mutableStateOf(false) }
    var showRaisingPortal by remember { mutableStateOf(false) }
    var showPivotDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    // Onboarding triggers on fresh game week 1 with default start
    LaunchedEffect(state.week, state.users) {
        if (state.week == 1 && state.users == 0L) {
            showOnboarding = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. TOP BAR: CEO Identity & Title ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dynamic 3D CEO Avatar Badge
                val ceoAvatarRes = when (state.ceoMood) {
                    "CELEBRATING" -> R.drawable.img_ceo_celebrating
                    "STRESSED" -> R.drawable.img_ceo_stressed
                    "DRINKING" -> R.drawable.img_ceo_drinking
                    else -> R.drawable.img_ceo_working
                }
                val moodBorderColor = when (state.ceoMood) {
                    "CELEBRATING" -> Emerald400
                    "STRESSED" -> Rose400
                    "DRINKING" -> Amber400
                    else -> Indigo500
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(White5)
                        .border(2.dp, moodBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = ceoAvatarRes),
                        contentDescription = "CEO 3D Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column {
                    Text(
                        text = state.ceoName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = state.ceoStage.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Indigo400,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Indigo10)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Week ${state.week}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Indigo400,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Help/Rules Button
                IconButton(
                    onClick = { showRulesDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(White5, CircleShape)
                        .border(1.dp, White10, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rules & Guide",
                        tint = TextMuted
                    )
                }

                // Restart Button
                IconButton(
                    onClick = { showOnboarding = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(White5, CircleShape)
                        .border(1.dp, White10, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Simulator",
                        tint = TextMuted
                    )
                }
            }
        }

        // --- 2. MAIN COMPANY DASHBOARD CARD (Frosted Glass) ---
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${state.companyName} Valuation",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.formatCurrency(state.valuation),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TextWhite,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val growthPercent = state.valuationGrowth * 100
                        val growthSign = if (growthPercent >= 0) "+" else ""
                        val badgeBg = if (growthPercent >= 0) Emerald20 else Rose400.copy(alpha = 0.15f)
                        val badgeText = if (growthPercent >= 0) Emerald400 else Rose400

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$growthSign${String.format("%.1f", growthPercent)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = badgeText
                            )
                        }
                        Text(
                            text = "vs last week",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 11.sp
                        )
                    }
                }

                // Sector Logo
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TextWhite)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (state.sector) {
                            Sector.AI -> "ΑI"
                            Sector.SAAS -> "☁"
                            Sector.BIOTECH -> "🧬"
                            Sector.FINTECH -> "💳"
                            Sector.WEB3 -> "⛓"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = BgSlate,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Valuation Grid Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cash Stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(16.dp))
                        .border(1.dp, White10, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "CASH RESERVES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.formatCurrency(state.cash),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (state.cash < 10_000.0) Rose400 else Emerald400
                    )
                }

                // Burn & Runway Stats
                val engineers = state.employees.count { it.role == "Engineer" }
                val totalSalaries = state.employees.sumOf { it.salary }
                val baseRent = when (state.ceoStage) {
                    CEOStage.SOLO_FOUNDER -> 0.0
                    CEOStage.SEED_STAGE -> 500.0
                    CEOStage.SERIES_A -> 2000.0
                    CEOStage.SERIES_B -> 6000.0
                    CEOStage.SERIES_C -> 20000.0
                    CEOStage.TECH_TITAN -> 60000.0
                }
                val weeklyRevenue = state.users * (state.sector.revenuePerUser / 4.0)
                val totalWeeklyBurn = baseRent + (totalSalaries / 4.0)
                val netCashflow = weeklyRevenue - totalWeeklyBurn

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(16.dp))
                        .border(1.dp, White10, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "WEEKLY BURNOUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.formatCurrency(totalWeeklyBurn),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Rose400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product & Users Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product quality
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Product Quality",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", state.productQuality)}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = Indigo400,
                        fontWeight = FontWeight.Black
                    )
                }

                // Users count
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Users",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.formatNumber(state.users),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // --- 3. GAME OVER OVERLAY / ALERTS / DECISIONS ---
        if (state.isGameOver) {
            GameOverPanel(viewModel) {
                showOnboarding = true
            }
        } else if (state.isIPOExited) {
            ExitSuccessPanel(state, viewModel) {
                showOnboarding = true
            }
        } else {
            // Event Decision Active Overlay
            state.activeDecisionId?.let { id ->
                val activeEvent = viewModel.activeEventDecision
                if (activeEvent != null) {
                    ActiveDecisionCard(activeEvent) { optionA ->
                        viewModel.makeDecision(optionA)
                    }
                }
            }

            // Normal Feed & Screen area
            if (state.activeDecisionId == null) {
                // Chronological Live News Ticker Card
                BoardTickerCard(state)

                // --- 4. PRIMARY NAVIGATION SCREENS ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        "Office" -> OfficeTabContent(state, viewModel, { showRaisingPortal = true }) { showHiringPortal = true }
                        "Market" -> MarketTabContent(state, viewModel)
                        "Analytics" -> AnalyticsTabContent(state, viewModel)
                        "Perks" -> PerksTabContent(state, viewModel)
                        "Exit" -> ExitTabContent(state, viewModel)
                    }
                }

                // --- 5. ACTION SHORCUT BAR (Raise, Team, Pivot) ---
                ActionBarRow(
                    onRaiseClick = { showRaisingPortal = true },
                    onTeamClick = { showHiringPortal = true },
                    onPivotClick = { showPivotDialog = true }
                )
            }
        }

        // --- 6. NAVIGATION RAIL ---
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }

    // --- ONBOARDING DIALOG ---
    if (showOnboarding) {
        OnboardingDialog(
            onSubmit = { ceo, company, sector ->
                viewModel.resetGame(ceo, company, sector)
                showOnboarding = false
            },
            onDismiss = { showOnboarding = false },
            isFirstStart = state.users == 0L
        )
    }

    // --- HIRING PORTAL SHEET ---
    if (showHiringPortal) {
        Dialog(
            onDismissRequest = { showHiringPortal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            FrostedGlassSheetFrame(
                title = "Talent Pool Recruiting",
                onClose = { showHiringPortal = false }
            ) {
                HiringSheetContent(candidates, state) { candidate ->
                    viewModel.hireEmployee(candidate)
                }
            }
        }
    }

    // --- CAPITAL RAISING SHEET ---
    if (showRaisingPortal) {
        Dialog(
            onDismissRequest = { showRaisingPortal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            FrostedGlassSheetFrame(
                title = "Pitch VCs & Fundraise",
                onClose = { showRaisingPortal = false }
            ) {
                RaisingSheetContent(state.simFundingOffers, state, viewModel) { offer ->
                    viewModel.acceptSimFundingOffer(offer.id)
                    showRaisingPortal = false
                }
            }
        }
    }

    // --- PIVOT SECTOR DIALOG ---
    if (showPivotDialog) {
        PivotSectorDialog(
            state = state,
            onConfirm = { newSector ->
                viewModel.pivotSector(newSector)
                showPivotDialog = false
            },
            onDismiss = { showPivotDialog = false }
        )
    }

    // --- RULES & GUIDE DIALOG ---
    if (showRulesDialog) {
        RulesGuideDialog { showRulesDialog = false }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun BoardTickerCard(state: GamePrefs.GameState) {
    val latestNews = state.newsFeed.lastOrNull() ?: return
    
    val borderColor = if (latestNews.isAlert) Rose400.copy(alpha = 0.4f) else Indigo500.copy(alpha = 0.3f)
    val containerBg = if (latestNews.isAlert) Rose400.copy(alpha = 0.08f) else Indigo10

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (latestNews.isAlert) Rose400 else Indigo400)
        )
        Text(
            text = "${latestNews.source}: ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = if (latestNews.isAlert) Rose400 else Indigo400
        )
        Text(
            text = latestNews.headline,
            style = MaterialTheme.typography.bodySmall,
            color = TextLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActiveDecisionCard(event: EventDecision, onOptionSelected: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_decision_card"),
        colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, Indigo400)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    tint = Amber400,
                    contentDescription = "Board Intervention"
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                lineHeight = 20.sp
            )

            Divider(color = Color.White.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option A
                Button(
                    onClick = { onOptionSelected(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.optionAText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                }

                // Option B
                Button(
                    onClick = { onOptionSelected(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = White10),
                    border = BorderStroke(1.dp, White20),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.optionBText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OfficeTabContent(
    state: GamePrefs.GameState,
    viewModel: GameViewModel,
    onRaiseClick: () -> Unit,
    onHiringClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Startup Office",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
            Text(
                text = "${state.employees.size} Employees",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }

        // Beautiful visual office co-working header image to look like a game!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_office_header),
                    contentDescription = "My Startup Office Header",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Premium gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 80f
                            )
                        )
                )
                // Overlay text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Text(
                        text = "CEO: ${state.ceoName} | Level ${state.founderLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Interactive 3D CEO Avatar & Mood Studio Card ---
        val activeAvatarRes = when (state.ceoMood) {
            "CELEBRATING" -> R.drawable.img_ceo_celebrating
            "STRESSED" -> R.drawable.img_ceo_stressed
            "DRINKING" -> R.drawable.img_ceo_drinking
            else -> R.drawable.img_ceo_working
        }

        val activeMoodLabel = when (state.ceoMood) {
            "CELEBRATING" -> "🎉 CELEBRATING"
            "STRESSED" -> "🔥 CRITICAL CRUNCH"
            "DRINKING" -> "☕ ESPRESSO HIGH"
            else -> "💻 DEEP WORKING"
        }

        val moodColor = when (state.ceoMood) {
            "CELEBRATING" -> Emerald400
            "STRESSED" -> Rose400
            "DRINKING" -> Amber400
            else -> Indigo500
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White5),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, White10)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Interactive 3D Avatar Image & Energy
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, moodColor, RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                        ) {
                            Image(
                                painter = painterResource(id = activeAvatarRes),
                                contentDescription = "CEO Interactive Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Energy Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                tint = moodColor,
                                modifier = Modifier.size(14.dp),
                                contentDescription = "Energy Icon"
                            )
                            Text(
                                text = "ENERGY: ${state.ceoEnergy}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.ceoEnergy > 50) Emerald400 else if (state.ceoEnergy > 20) Amber400 else Rose400,
                                fontSize = 10.sp
                            )
                        }
                        
                        // Energy Linear Indicator
                        LinearProgressIndicator(
                            progress = { state.ceoEnergy / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = if (state.ceoEnergy > 50) Emerald400 else if (state.ceoEnergy > 20) Amber400 else Rose400,
                            trackColor = White10
                        )
                    }

                    // Right Column: Info & Action Buttons
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CEO INTERACTIVE AVATAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = state.ceoName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            // Dynamic Status Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(moodColor.copy(alpha = 0.15f))
                                    .border(1.dp, moodColor.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = activeMoodLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = moodColor,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Text(
                            text = "Interact with your 3D avatar to run real-time startup operations and boost metrics instantly:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight,
                            fontSize = 11.sp
                        )

                        // Studio Action Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Deep Work
                            Button(
                                onClick = { viewModel.executeCEODeepWork() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💻 FOCUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 9.sp)
                                    Text("+2 Quality", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                                }
                            }

                            // Espresso
                            Button(
                                onClick = { viewModel.executeCEODrinkEspresso() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Amber400.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Amber400.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("☕ ESPRESSO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 9.sp)
                                    Text("100% Energy", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Morale Bonus
                            Button(
                                onClick = { viewModel.executeCEOGiveBonus() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Emerald400.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = state.cash >= 1000.0
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💰 BONUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (state.cash >= 1000.0) TextWhite else TextMuted, fontSize = 9.sp)
                                    Text("-$1k | +XP", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                                }
                            }

                            // Crunch
                            Button(
                                onClick = { viewModel.executeCEOOvertimeCrunch() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose400.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Rose400.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = state.ceoEnergy >= 25
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔥 CRUNCH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (state.ceoEnergy >= 25) TextWhite else TextMuted, fontSize = 9.sp)
                                    Text("-25E | +Users", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.employees.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(20.dp))
                    .border(1.dp, White10, RoundedCornerShape(20.dp))
                    .clickable { onHiringClick() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp),
                        contentDescription = "Hiring"
                    )
                    Text(
                        text = "Your office is currently empty!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = "Click to browse candidates and hire your first Engineer, Marketer, or Product Manager.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Employees list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.employees) { emp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White5, RoundedCornerShape(16.dp))
                            .border(1.dp, White10, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = when (emp.role) {
                                            "Engineer" -> Indigo500
                                            "Marketer" -> Emerald400
                                            else -> Amber400
                                        },
                                        shape = CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = when (emp.role) {
                                            "Engineer" -> R.drawable.img_char_engineer
                                            "Marketer" -> R.drawable.img_char_marketer
                                            else -> R.drawable.img_char_pm
                                        }
                                    ),
                                    contentDescription = "${emp.role} Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column {
                                Text(
                                    text = emp.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${emp.skillLevel} ${emp.role} (Skill: ${emp.skillScore})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${viewModel.formatCurrency(emp.salary)}/mo",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Rose400,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.fireEmployee(emp) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fire Employee",
                                    tint = Rose400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketTabContent(state: GamePrefs.GameState, viewModel: GameViewModel) {
    val regions = listOf(
        MarketRegion("North America", true, 0.0, 1_000_000, 1.0),
        MarketRegion("Europe Expansion", state.unlockedRegions.contains("Europe Expansion"), 25_000.0, 3_000_000, 1.8),
        MarketRegion("Asia-Pacific Core", state.unlockedRegions.contains("Asia-Pacific Core"), 100_000.0, 10_000_000, 2.5),
        MarketRegion("Emerging LatAm & Africa", state.unlockedRegions.contains("Emerging LatAm & Africa"), 500_000.0, 50_000_000, 4.0)
    )

    var activeSubTab by remember { mutableStateOf("Intelligence") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Custom segmented sub-tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White5, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Regions", "AI Intelligence & Rivals").forEach { tab ->
                val isSelected = (tab == "Regions" && activeSubTab == "Regions") || (tab == "AI Intelligence & Rivals" && activeSubTab == "Intelligence")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Indigo500 else Color.Transparent)
                        .clickable { activeSubTab = if (tab == "Regions") "Regions" else "Intelligence" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextWhite else TextMuted
                    )
                }
            }
        }

        if (activeSubTab == "Regions") {
            Text(
                text = "Global Market Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(regions) { reg ->
                    val isUnlocked = reg.isUnlocked
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isUnlocked) Indigo500.copy(alpha = 0.04f) else White5,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isUnlocked) Indigo500.copy(alpha = 0.3f) else White10,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = reg.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) TextWhite else TextMuted
                                )
                                if (isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        tint = Emerald400,
                                        contentDescription = "Unlocked",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Market Size: ${viewModel.formatNumber(reg.userCapacity)} cap • Acquisition multiplier: x${reg.userAcquisitionMultiplier}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        if (!isUnlocked) {
                            Button(
                                onClick = { viewModel.unlockRegion(reg.name, reg.unlockCost) },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                                enabled = state.cash >= reg.unlockCost,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Unlock (${viewModel.formatCurrency(reg.unlockCost)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // "Intelligence & Rivals" tab content
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Active Market Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }

                if (state.marketEvents.isEmpty() && state.marketTrends.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = White5),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    tint = Emerald400,
                                    contentDescription = "Stable Market",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Market Conditions Stable",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "No active macroeconomic crises or regulations affecting your company this week. Organic baseline scaling is active.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Render Trends
                    items(state.marketTrends) { trend ->
                        val trendColor = if (trend.trendType == "BULLISH") Emerald400 else Amber400
                        Card(
                            colors = CardDefaults.cardColors(containerColor = White5),
                            border = BorderStroke(1.dp, trendColor.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(trendColor.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (trend.trendType == "BULLISH") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        tint = trendColor,
                                        modifier = Modifier.size(20.dp),
                                        contentDescription = trend.trendType
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = trend.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "${trend.durationWeeks} weeks left",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                    Text(
                                        text = "Sector Impact: ${trend.sectorImpact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = trendColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = trend.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Render Events
                    items(state.marketEvents) { ev ->
                        val borderCol = when (ev.severity) {
                            "CRITICAL" -> Rose400
                            "WARNING" -> Amber400
                            else -> Emerald400
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = White5),
                            border = BorderStroke(1.5.dp, borderCol.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(borderCol, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ev.type.name.replace("_", " "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Text(
                                            text = ev.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Text(
                                        text = "${ev.effectDurationWeeks}w left",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ev.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Multipliers list
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (ev.userGrowthMultiplier != 1.0) {
                                        val isPositive = ev.userGrowthMultiplier > 1.0
                                        BadgeMultiplier(
                                            label = "Growth",
                                            value = "x${ev.userGrowthMultiplier}",
                                            isPositive = isPositive
                                        )
                                    }
                                    if (ev.valuationMultiplier != 1.0) {
                                        val isPositive = ev.valuationMultiplier > 1.0
                                        BadgeMultiplier(
                                            label = "Valuation",
                                            value = "x${ev.valuationMultiplier}",
                                            isPositive = isPositive
                                        )
                                    }
                                    if (ev.salaryMultiplier != 1.0) {
                                        val isPositive = ev.salaryMultiplier < 1.0
                                        BadgeMultiplier(
                                            label = "Salaries",
                                            value = "x${ev.salaryMultiplier}",
                                            isPositive = isPositive
                                        )
                                    }
                                    if (ev.cashMultiplier != 1.0) {
                                        val isPositive = ev.cashMultiplier > 1.0
                                        BadgeMultiplier(
                                            label = "Cash Flow",
                                            value = "x${ev.cashMultiplier}",
                                            isPositive = isPositive
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Competitors & Rivals
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rivals & Competitors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }

                val rivals = state.competitors.ifEmpty { com.example.engine.AIPoweredMarketEngine.generateInitialCompetitors(state.sector) }
                items(rivals) { rival ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, White10),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rival.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = rival.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = "Valuation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(rival.valuation),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Indigo400
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = White10)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Product Quality & Market Share Rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Product Quality",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${String.format("%.1f", rival.productQuality)}/100",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = (rival.productQuality / 100.0).toFloat(),
                                        color = Indigo500,
                                        trackColor = White10,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Market Share",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${String.format("%.1f", rival.marketShare)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = (rival.marketShare / 100.0).toFloat(),
                                        color = Emerald400,
                                        trackColor = White10,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }

                            if (rival.products.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Products Launched:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rival.products.forEach { prod ->
                                        Box(
                                            modifier = Modifier
                                                .background(White10, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = prod,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeMultiplier(label: String, value: String, isPositive: Boolean) {
    val tintColor = if (isPositive) Emerald400 else Rose400
    Box(
        modifier = Modifier
            .background(tintColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(1.dp, tintColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = tintColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}


@Composable
fun AnalyticsTabContent(state: GamePrefs.GameState, viewModel: GameViewModel) {
    val weeklyRevenue = state.users * (state.sector.revenuePerUser / 4.0)
    val annualizedRevenue = weeklyRevenue * 52.0
    val totalSalaries = state.employees.sumOf { it.salary }
    val baseRent = when (state.ceoStage) {
        CEOStage.SOLO_FOUNDER -> 0.0
        CEOStage.SEED_STAGE -> 500.0
        CEOStage.SERIES_A -> 2000.0
        CEOStage.SERIES_B -> 6000.0
        CEOStage.SERIES_C -> 20000.0
        CEOStage.TECH_TITAN -> 60000.0
    }
    val totalWeeklyBurn = baseRent + (totalSalaries / 4.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Key Business Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ARR (Annual Recurring Revenue)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.formatCurrency(annualizedRevenue),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Emerald400
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Current Sector Multiple: x${when (state.sector) {
                        Sector.SAAS -> 12
                        Sector.AI -> 25
                        Sector.BIOTECH -> 18
                        Sector.FINTECH -> 15
                        Sector.WEB3 -> 8
                    }} of ARR",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Efficiency stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(16.dp))
                        .border(1.dp, White10, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "CAPITAL EFFICIENCY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val efficiency = if (totalWeeklyBurn > 0) weeklyRevenue / totalWeeklyBurn else 100.0
                    Text(
                        text = "${String.format("%.1f", efficiency)}x",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = if (efficiency >= 1.0) Emerald400 else Amber400,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dilution stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(White5, RoundedCornerShape(16.dp))
                        .border(1.dp, White10, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "VC DILUTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", state.totalVCDilution)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = if (state.totalVCDilution < 35.0) Emerald400 else Rose400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Capital Investments Raised",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.formatCurrency(state.totalInvestmentRaised),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Indigo400
                )
            }
        }
    }
}

@Composable
fun ExitTabContent(state: GamePrefs.GameState, viewModel: GameViewModel) {
    val currentValuation = state.valuation
    val ipoEligible = currentValuation >= 100_000_000.0

    // Stateful values for custom board proposals
    var selectedProposalType by remember { mutableStateOf("MARKETING_BOOST") }
    var proposalTitle by remember { mutableStateOf("Expand Growth Initiatives") }
    var proposalDesc by remember { mutableStateOf("Requesting authorization for budget to launch an aggressive marketing acquisition push.") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Dashboard Title
        item {
            Column {
                Text(
                    text = "Boardroom & Finance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Text(
                    text = "Manage your capitalization table, board members, debts, and vote on key strategic proposals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // 1. Capitalization Table Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White5),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, White10)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capitalization Table",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Text(
                            text = "Delaware C-Corp",
                            style = MaterialTheme.typography.labelSmall,
                            color = Indigo400,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = White10)

                    val shareholders = state.capTable.ifEmpty {
                        listOf(
                            com.example.engine.Shareholder(id = "ceo", name = state.ceoName, equityShares = 9_000_000L, ownershipPercent = 90.0, isFounder = true),
                            com.example.engine.Shareholder(id = "employee_pool", name = "Employee Option Pool", equityShares = 1_000_000L, ownershipPercent = 10.0, isFounder = false)
                        )
                    }

                    shareholders.forEach { holder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = holder.name + (if (holder.isFounder) " (Founder)" else ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${viewModel.formatNumber(holder.equityShares)} shares",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "${String.format("%.2f", holder.ownershipPercent)}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = if (holder.isFounder) Emerald400 else TextLight
                            )
                        }
                        HorizontalDivider(color = White5)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Authorized Shares",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = viewModel.formatNumber(state.totalAuthorizedShares),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                }
            }
        }

        // 2. Board of Directors
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White5),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, White10)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Board of Directors",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )

                    HorizontalDivider(color = White10)

                    val board = state.boardMembers.ifEmpty {
                        listOf(
                            com.example.engine.SimBoardMember(id = "ceo", name = state.ceoName, representing = "Founder", votingPower = 51.0, supportiveness = 100.0),
                            com.example.engine.SimBoardMember(id = "indie", name = "Independent Seat", representing = "Independent", votingPower = 49.0, supportiveness = 75.0)
                        )
                    }

                    board.forEach { member ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${member.representing} • Voting Power: ${member.votingPower.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "${member.supportiveness.toInt()}% Support",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (member.supportiveness >= 75.0) Emerald400 else if (member.supportiveness >= 50.0) Amber400 else Rose400
                                )
                            }
                            LinearProgressIndicator(
                                progress = (member.supportiveness / 100.0).toFloat(),
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (member.supportiveness >= 75.0) Emerald400 else if (member.supportiveness >= 50.0) Amber400 else Rose400,
                                trackColor = White10
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // 3. Strategic Proposals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White5),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, White10)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Propose Strategic Actions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )

                    HorizontalDivider(color = White10)

                    // Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val proposalOptions = listOf(
                            Triple("MARKETING_BOOST", "Growth", "Authorizes $10k marketing budget"),
                            Triple("HOST_VIP_GALA", "Gala", "Authorizes $25k valuation boost"),
                            Triple("EXEC_SEARCH", "Recruit", "Authorizes $8k headhunt search")
                        )

                        proposalOptions.forEach { (type, label, details) ->
                            val isSelected = selectedProposalType == type
                            Button(
                                onClick = {
                                    selectedProposalType = type
                                    when (type) {
                                        "MARKETING_BOOST" -> {
                                            proposalTitle = "Expand Growth Initiatives"
                                            proposalDesc = "Requesting authorization for budget to launch an aggressive marketing acquisition push."
                                        }
                                        "HOST_VIP_GALA" -> {
                                            proposalTitle = "Host High-Profile Gala"
                                            proposalDesc = "Hosting a networking gala to mingle with top VCs and Silicon Valley tech-angels."
                                        }
                                        "EXEC_SEARCH" -> {
                                            proposalTitle = "Initiate Executive Hunt"
                                            proposalDesc = "Requesting capital approval to recruit top-tier technical and engineering executives."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Indigo500 else White10
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Indigo400 else White10),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextWhite else TextMuted
                                )
                            }
                        }
                    }

                    // Selected Details Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White5, RoundedCornerShape(12.dp))
                            .border(1.dp, White5, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = proposalTitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = proposalDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        val costText = when (selectedProposalType) {
                            "MARKETING_BOOST" -> "Cost: $10,000"
                            "HOST_VIP_GALA" -> "Cost: $25,000"
                            "EXEC_SEARCH" -> "Cost: $8,000"
                            else -> "Cost: $0"
                        }
                        Text(
                            text = costText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber400,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.submitStrategicProposal(selectedProposalType, proposalTitle, proposalDesc)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Submit to Board for Voting",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        // 4. Outstanding Convertible Notes, SAFEs & Debt
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White5),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, White10)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Outstanding Convertible Notes & SAFEs",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )

                    HorizontalDivider(color = White10)

                    if (state.convertibleNotes.isEmpty()) {
                        Text(
                            text = "No active SAFEs or Convertible Notes outstanding.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        state.convertibleNotes.forEach { note ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(White5, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${note.investorName} - ${note.type.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Principal: ${viewModel.formatCurrency(note.principalAmount)} • " +
                                            "Cap: ${viewModel.formatCurrency(note.valuationCap)} • " +
                                            "Discount: ${(note.discountRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Active Loan / Debt Financing",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )

                    HorizontalDivider(color = White10)

                    if (state.debts.isEmpty()) {
                        Text(
                            text = "No active bank loans or debt facilities outstanding.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        state.debts.forEach { debt ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(White5, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${debt.lenderName} Debt Facility",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${debt.weeksRemaining} wks left",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Amber400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Remaining Balance: ${viewModel.formatCurrency(debt.remainingBalance)} • APR: ${(debt.annualInterestRate * 100).toInt()}% • Weekly Payment: ${viewModel.formatCurrency(debt.weeklyRepayment)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // 5. Acquisition Payout / Buyouts
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(20.dp))
                    .border(1.dp, White10, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Acquisition Offer (M&A Buyout)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                val buyers = listOf("Google", "Meta", "Microsoft", "Apple", "Oracle")
                val offerValue = currentValuation * 1.25 // Standard buyout markup

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White5, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = buyers.first(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Offer: ${viewModel.formatCurrency(offerValue)} (1.25x Multiple)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = { viewModel.acceptAcquisition(offerValue, buyers.first()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Accept Buyout", color = BgSlate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 6. NASDAQ Public Listing (IPO)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (ipoEligible) Indigo500.copy(alpha = 0.05f) else White5,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        if (ipoEligible) Indigo400.copy(alpha = 0.4f) else White10,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    tint = if (ipoEligible) Indigo400 else TextMuted,
                    modifier = Modifier.size(44.dp),
                    contentDescription = "IPO"
                )

                Text(
                    text = "Initial Public Offering (IPO)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )

                Text(
                    text = "To list publicly on NASDAQ, your company needs at least $100M valuation. Achieve hyper-growth to ring the bell!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { viewModel.triggerIPO() },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    enabled = ipoEligible,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (ipoEligible) "Ring NASDAQ Bell (IPO)" else "Valuation too low to IPO",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverPanel(viewModel: GameViewModel, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .border(2.dp, Rose400, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "COMPANY LIQUIDATION",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Rose400,
                letterSpacing = 1.sp
            )

            Text(
                text = "Bankruptcy declared! Your cash balance reached $0. The Silicon Valley venture dream has come to an end.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Rose400),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Try Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExitSuccessPanel(state: GamePrefs.GameState, viewModel: GameViewModel, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
            .border(2.dp, Emerald400, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "🦄 BILLION-DOLLAR EXIT! 🦄",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Emerald400,
                textAlign = TextAlign.Center
            )

            val finalPayout = state.valuation * (1.0 - (state.totalVCDilution / 100.0))

            Text(
                text = "Congratulations, ${state.ceoName}! You successfully steered ${state.companyName} to a successful Silicon Valley exit at a final valuation of ${viewModel.formatCurrency(state.valuation)}!\n\nYour net payout: ${viewModel.formatCurrency(finalPayout)} after VC equity dilution.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Start New Venture", color = BgSlate, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionBarRow(
    onRaiseClick: () -> Unit,
    onTeamClick: () -> Unit,
    onPivotClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Raise Button
        Button(
            onClick = onRaiseClick,
            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "Raise")
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "RAISE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
        }

        // Team Button
        Button(
            onClick = onTeamClick,
            colors = ButtonDefaults.buttonColors(containerColor = White10),
            border = BorderStroke(1.dp, White20),
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Group, contentDescription = "Team", tint = TextWhite)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "TEAM", style = MaterialTheme.typography.labelMedium, color = TextWhite, fontWeight = FontWeight.Black)
        }

        // Pivot Button
        Button(
            onClick = onPivotClick,
            colors = ButtonDefaults.buttonColors(containerColor = White10),
            border = BorderStroke(1.dp, White20),
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Pivot", tint = TextWhite)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "PIVOT", style = MaterialTheme.typography.labelMedium, color = TextWhite, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White5, RoundedCornerShape(20.dp))
            .border(1.dp, White10, RoundedCornerShape(20.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            Triple("Office", Icons.Default.Home, "Office"),
            Triple("Market", Icons.Default.Public, "Market"),
            Triple("Analytics", Icons.Default.BarChart, "Analytics"),
            Triple("Perks", Icons.Default.Star, "Perks"),
            Triple("Exit", Icons.Default.ExitToApp, "Boardroom")
        )

        navItems.forEach { (tab, icon, label) ->
            val isSelected = selectedTab == tab
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Indigo400 else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Indigo400 else TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// --- SHEETS AND FRAMEWORK DIALOGS ---

@Composable
fun FrostedGlassSheetFrame(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgSlate.copy(alpha = 0.95f))
            .border(2.dp, White10)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, tint = TextWhite, contentDescription = "Close")
                }
            }

            Divider(color = White10)

            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

@Composable
fun HiringSheetContent(candidates: List<Employee>, state: GamePrefs.GameState, onHire: (Employee) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Candidates available this week:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextLight
        )

        if (candidates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White5, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No candidates left! Tap 'Next Week' to generate a fresh talent pool.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(candidates) { candidate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White5, RoundedCornerShape(16.dp))
                            .border(1.dp, White10, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = when (candidate.role) {
                                            "Engineer" -> Indigo500
                                            "Marketer" -> Emerald400
                                            else -> Amber400
                                        },
                                        shape = CircleShape
                                    )
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = when (candidate.role) {
                                            "Engineer" -> R.drawable.img_char_engineer
                                            "Marketer" -> R.drawable.img_char_marketer
                                            else -> R.drawable.img_char_pm
                                        }
                                    ),
                                    contentDescription = "${candidate.role} Candidate Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column {
                                Text(
                                    text = candidate.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${candidate.skillLevel} ${candidate.role} (Skill score: ${candidate.skillScore})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { onHire(candidate) },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Hire ($${candidate.salary.toInt()}/mo)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaisingSheetContent(
    offers: List<com.example.engine.SimInvestmentOffer>,
    state: GamePrefs.GameState,
    viewModel: GameViewModel,
    onAcceptOffer: (com.example.engine.SimInvestmentOffer) -> Unit
) {
    var selectedOfferForNegotiation by remember { mutableStateOf<com.example.engine.SimInvestmentOffer?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pitch Deck Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White5, RoundedCornerShape(16.dp))
                .border(1.dp, White10, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pitch Deck Score: ${state.pitchDeckQuality.toInt()}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Text(
                    text = "Higher score triggers larger offers and better valuations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.upgradePitchDeck() },
                enabled = state.cash >= 5000.0 && state.pitchDeckQuality < 100.0,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.pitchDeckQuality >= 100.0) "Max Grade" else "Upgrade ($5k)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Active Funding & Financing Term Sheets:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextLight
        )

        if (offers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No funding offers available this week. Tap 'Next Week' to let your metrics compound or upgrade your Pitch Deck!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(offers) { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = White5),
                        border = BorderStroke(1.dp, if (selectedOfferForNegotiation?.id == offer.id) Indigo400 else White10),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${offer.type.displayName} - ${offer.investorName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = buildString {
                                            append("Amount: ${viewModel.formatCurrency(offer.amount)}")
                                            offer.impliedValuation?.let { append("\nImplied Valuation: ${viewModel.formatCurrency(it)}") }
                                            offer.valuationCap?.let { append("\nValuation Cap: ${viewModel.formatCurrency(it)}") }
                                            offer.discountRate?.let { append("\nDiscount: ${(it * 100).toInt()}%") }
                                            offer.interestRate?.let { append("\nInterest Rate: ${(it * 100).toInt()}% APR") }
                                            offer.termWeeks?.let { append("\nTerm: $it weeks") }
                                            append("\nInvestor Supportiveness: ${offer.supportiveness.toInt()}/100")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = { onAcceptOffer(offer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Accept",
                                        color = BgSlate,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            HorizontalDivider(color = White10)
                            if (selectedOfferForNegotiation?.id == offer.id) {
                                Text(
                                    text = "Choose a negotiation strategy (Risk: investor might walk away!):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo400
                                )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.negotiateFundingOffer(offer.id, "AGGRESSIVE_VALUATION") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Rose400.copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, Rose400),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Aggressive (Valuation)", color = Rose400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.negotiateFundingOffer(offer.id, "FRIENDLY_RELATIONS") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500.copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, Indigo400),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Friendly (Support)", color = Indigo400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.negotiateFundingOffer(offer.id, "LOW_DILUTION") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Emerald20),
                                            border = BorderStroke(1.dp, Emerald400),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Low Dilution (Cap)", color = Emerald400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { selectedOfferForNegotiation = offer },
                                        colors = ButtonDefaults.buttonColors(containerColor = White10),
                                        border = BorderStroke(1.dp, White20),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Negotiate Term Sheet", color = TextLight, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun PivotSectorDialog(
    state: GamePrefs.GameState,
    onConfirm: (Sector) -> Unit,
    onDismiss: () -> Unit
) {
    val sectors = Sector.values()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pivot Business Focus",
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "A strategic pivot will focus your product. It costs $15,000 for restructuring and resets 25% of product quality from migration legacy friction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Divider(color = White10)

                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sectors) { sector ->
                        val isCurrent = state.sector == sector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) Indigo10 else White5,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isCurrent) Indigo500 else White10,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onConfirm(sector) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sector.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Indigo400 else TextWhite
                                )
                                Text(
                                    text = "ARR Multiple: x${sector.revenuePerUser.toInt()} • Tech Complexity: ${sector.techDifficulty}/100",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextMuted)
            }
        },
        containerColor = BgSlate,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun OnboardingDialog(
    onSubmit: (String, String, Sector) -> Unit,
    onDismiss: () -> Unit,
    isFirstStart: Boolean
) {
    var ceoName by remember { mutableStateOf("Sarah Jenkins") }
    var companyName by remember { mutableStateOf("NeuraLinker") }
    var selectedSector by remember { mutableStateOf(Sector.AI) }

    Dialog(
        onDismissRequest = { if (!isFirstStart) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isFirstStart, dismissOnClickOutside = !isFirstStart)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSlate, RoundedCornerShape(28.dp))
                .border(2.dp, White20, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isFirstStart) "Welcome, Founder! 🚀" else "Launch New Venture ♻",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Define your profile, company name, and industry focus. Good luck scaling to a billion dollars!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(White5)
                        .border(2.dp, Indigo500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ceo_working),
                        contentDescription = "CEO Preview Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = ceoName,
                    onValueChange = { ceoName = it },
                    label = { Text("Your CEO Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = White20,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Your Company Name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = White20,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Target Tech Sector:", style = MaterialTheme.typography.labelMedium, color = TextLight, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Sector.values().take(3).forEach { sector ->
                            val active = selectedSector == sector
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (active) Indigo10 else White5, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (active) Indigo400 else White10, RoundedCornerShape(8.dp))
                                    .clickable { selectedSector = sector }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sector.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (active) Indigo400 else TextMuted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Sector.values().drop(3).forEach { sector ->
                            val active = selectedSector == sector
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (active) Indigo10 else White5, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (active) Indigo400 else White10, RoundedCornerShape(8.dp))
                                    .clickable { selectedSector = sector }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sector.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (active) Indigo400 else TextMuted
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { onSubmit(ceoName, companyName, selectedSector) },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Begin Venture", fontWeight = FontWeight.Bold, color = TextWhite)
                }

                if (!isFirstStart) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun RulesGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Startup CEO Handbook", fontWeight = FontWeight.Black, color = TextWhite)
        },
        text = {
            Column(
                modifier = Modifier.height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Goal: Grow your company valuation from $150k to $1B+ Unicorn status and ring the bell for a NASDAQ IPO exit!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400
                )
                Text(
                    text = "• Engineers: Grow product quality index. Without engineers, products undergo code bit rot (-0.15 index points/week).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Text(
                    text = "• Marketers: Gain +250 organic users/mo per hire and amplify global baseline reach multipliers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Text(
                    text = "• Cash Flow: Balance your Burn Rate (salaries + office lease) against weekly revenue. If cash drops to $0, liquidation occurs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Text(
                    text = "• Funding Rounds: Negotiate term sheets to secure capital injections. Every round dilutes your final founder payout percentages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Text(
                    text = "• Pivots: Pivot your focus area to adjust growth scales and annualized revenue multiple multiples.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) {
                Text(text = "Back to Office")
            }
        },
        containerColor = BgSlate,
        shape = RoundedCornerShape(24.dp)
    )
}
