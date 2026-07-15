package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.engine.ProgressionData

@Composable
fun PerksTabContent(state: GamePrefs.GameState, viewModel: GameViewModel) {
    var activeSubTab by remember { mutableStateOf("Hub") } // "Hub", "Pass", "Customizer", "VIP"
    var showReceiptDialog by remember { mutableStateOf<String?>(null) } // holds receipt item name
    var showInterstitialAd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Title block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Progression & Perks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Text(
                    text = "Unlock cosmetic tiers & premium perks",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            // Tokens indicator
            Box(
                modifier = Modifier
                    .background(Indigo500.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, Indigo500.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Text(
                        text = "${state.cosmeticTokens} Tokens",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = Amber400
                    )
                }
            }
        }

        // Sub-tabs segment selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White5, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                "Hub" to "Hub & Daily",
                "Pass" to "Founder Pass",
                "Custom" to "Customizer",
                "VIP" to "VIP Shop"
            )
            tabs.forEach { (key, label) ->
                val isSelected = activeSubTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Indigo500 else Color.Transparent)
                        .clickable { activeSubTab = key }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextWhite else TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Render contents based on selection
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "Hub" -> HubSubTab(state, viewModel, onWatchAdClick = { showInterstitialAd = true })
                "Pass" -> PassSubTab(state, viewModel)
                "Custom" -> CustomizerSubTab(state, viewModel)
                "VIP" -> VipSubTab(state, viewModel) { itemName -> showReceiptDialog = itemName }
            }
        }

        // AdMob Mock Banner Ad at the bottom
        if (!state.hasRemovedAds) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, White10),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Amber400, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ad",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "AdMob Banner • Unlock cosmetic options for free!",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = { viewModel.subscribeToCEOClub() },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Remove Ads", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Interstitial Ad Dialog mockup
    if (showInterstitialAd) {
        Dialog(
            onDismissRequest = { showInterstitialAd = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Indigo500.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, Indigo500, CircleShape)
                            .size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            tint = Indigo300,
                            modifier = Modifier.size(40.dp),
                            contentDescription = "Playing ad"
                        )
                    }
                    Text(
                        text = "Simulated AdMob Reward Ad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Ads are strictly optional and help fund cosmetic customization updates. Standard competition rules apply.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    
                    var adCountdown by remember { mutableStateOf(5) }
                    LaunchedEffect(Unit) {
                        while (adCountdown > 0) {
                            kotlinx.coroutines.delay(1000)
                            adCountdown--
                        }
                    }

                    if (adCountdown > 0) {
                        Text(
                            text = "Reward unlocks in $adCountdown seconds...",
                            style = MaterialTheme.typography.labelMedium,
                            color = Amber400,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeSimulatedRewardedAd()
                                showInterstitialAd = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Claim Reward (+30 Tokens) & Close", fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Receipt Validation Dialog mockup
    showReceiptDialog?.let { itemName ->
        Dialog(onDismissRequest = { showReceiptDialog = null }) {
            var validationStep by remember { mutableStateOf(0) }
            val logs = remember { mutableStateListOf<String>() }

            LaunchedEffect(validationStep) {
                when (validationStep) {
                    0 -> {
                        logs.add("🚀 Initiating Google Play IAP Billing Flow...")
                        kotlinx.coroutines.delay(800)
                        validationStep = 1
                    }
                    1 -> {
                        logs.add("📝 Fetching JWS cryptosignature from Google Play Console...")
                        kotlinx.coroutines.delay(900)
                        validationStep = 2
                    }
                    2 -> {
                        logs.add("🔒 Sending transaction payload to Developer Server...")
                        kotlinx.coroutines.delay(800)
                        validationStep = 3
                    }
                    3 -> {
                        logs.add("🌐 developer-api.aistudio.com: validating signature (RSA-256)...")
                        kotlinx.coroutines.delay(1000)
                        validationStep = 4
                    }
                    4 -> {
                        logs.add("✅ Receipt signature matches Google root key!")
                        logs.add("📦 Delivering entitlement for: $itemName")
                        logs.add("🎉 Enjoy your cosmetic perks!")
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = BgSlate),
                border = BorderStroke(1.dp, White10),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = if (validationStep < 4) Indigo500 else Emerald400,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Receipt Server Validation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Log output console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .border(1.dp, White5, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (log.startsWith("✅") || log.startsWith("🎉")) Emerald400 else if (log.contains("developer-api")) Amber400 else TextLight
                                )
                            }
                        }
                    }

                    if (validationStep >= 4) {
                        Button(
                            onClick = { showReceiptDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "Please wait, validating secure cryptographic receipts to prevent pay-to-win manipulation...",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HubSubTab(
    state: GamePrefs.GameState,
    viewModel: GameViewModel,
    onWatchAdClick: () -> Unit
) {
    var doubleBonusWithAd by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Founder level and XP Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Indigo500, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = "Founder Level"
                                )
                            }
                            Column {
                                Text(
                                    text = "Founder Level ${state.founderLevel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${state.ceoName} • ${state.companyName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                        Text(
                            text = "XP: ${state.xp}/${state.founderLevel * 500}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Indigo300
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progressRatio = state.xp.toFloat() / (state.founderLevel * 500f)
                    LinearProgressIndicator(
                        progress = progressRatio.coerceIn(0f, 1f),
                        color = Indigo500,
                        trackColor = White10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advance weeks to earn baseline XP. Reaching milestones awards bonus tokens!",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Google Play Games & Cloud Save Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = White5),
                border = BorderStroke(1.dp, White10),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Cloud Integration & Services",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play Games Connect
                        Button(
                            onClick = { viewModel.togglePlayGamesSignIn() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isPlayGamesSignedIn) Emerald400 else Indigo500
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isPlayGamesSignedIn) Icons.Default.CheckCircle else Icons.Default.SportsEsports,
                                    tint = if (state.isPlayGamesSignedIn) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp),
                                    contentDescription = "GPlay"
                                )
                                Text(
                                    text = if (state.isPlayGamesSignedIn) "Play Synced" else "Link GPlay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isPlayGamesSignedIn) Color.Black else Color.White
                                )
                            }
                        }

                        // Cloud Save
                        Button(
                            onClick = { viewModel.triggerCloudSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = White10),
                            border = BorderStroke(1.dp, White5),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    tint = TextLight,
                                    modifier = Modifier.size(16.dp),
                                    contentDescription = "Cloud Save"
                                )
                                Text(
                                    text = "Cloud Backup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                            }
                        }
                    }

                    if (state.cloudSaveTimestamp > 0) {
                        Text(
                            text = "Last Cloud Backup: successfully verified by secure server checksum",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Daily login rewards tracker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = White5),
                border = BorderStroke(1.dp, White10),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Daily Login Rewards",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .background(Amber400.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Day ${state.lastClaimedDailyDay % 7} Claimed",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Daily Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..7).forEach { day ->
                            val isClaimed = day <= state.lastClaimedDailyDay
                            val isCurrent = day == (state.lastClaimedDailyDay % 7) + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isClaimed) Emerald400.copy(alpha = 0.1f)
                                        else if (isCurrent) Indigo500.copy(alpha = 0.15f)
                                        else White10,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isClaimed) Emerald400.copy(alpha = 0.4f)
                                        else if (isCurrent) Indigo500
                                        else White5,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "D$day",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isClaimed) Emerald400 else if (isCurrent) Indigo300 else TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (day) {
                                            2 -> "🎁"
                                            4 -> "💺"
                                            6 -> "🌿"
                                            7 -> "👑"
                                            else -> "🪙"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Checkbox(
                                checked = doubleBonusWithAd,
                                onCheckedChange = { doubleBonusWithAd = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Indigo500,
                                    uncheckedColor = White10
                                )
                            )
                            Text(
                                text = "Double reward with ad video",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Button(
                            onClick = { viewModel.claimDailyReward(doubleBonusWithAd) },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Claim Reward", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Achievements List Section
        item {
            Text(
                text = "Career Achievements",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val completedSet = state.completedAchievements.toSet()
        items(ProgressionData.ACHIEVEMENTS) { ach ->
            val isCompleted = completedSet.contains(ach.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isCompleted) Emerald400.copy(alpha = 0.04f) else White5,
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        1.dp,
                        if (isCompleted) Emerald400.copy(alpha = 0.25f) else White10,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ach.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Emerald400 else TextWhite
                    )
                    Text(
                        text = ach.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rewards: +${ach.xpReward} XP • +${ach.tokenReward} Tokens",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber400,
                            fontWeight = FontWeight.Bold
                        )
                        ach.cosmeticRewardId?.let { cid ->
                            Box(
                                modifier = Modifier
                                    .background(Indigo500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Cosmetic Unlock", fontSize = 8.sp, color = Indigo300, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        tint = Emerald400,
                        contentDescription = "Completed",
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        tint = TextMuted,
                        contentDescription = "Locked",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Rewarded Video Trigger Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.04f)),
                border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Play Optional Ad For Tokens",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Watch a 5-second simulated video ad to claim 30 free tokens. Zero pay-to-win advantage.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = onWatchAdClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Watch", modifier = Modifier.size(14.dp))
                            Text("Play", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassSubTab(state: GamePrefs.GameState, viewModel: GameViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Pass Progress Tracker header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, if (state.isFounderPassPremiumUnlocked) Amber400 else Indigo500.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (state.isFounderPassPremiumUnlocked) Amber400 else Indigo500, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardMembership,
                                    tint = if (state.isFounderPassPremiumUnlocked) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp),
                                    contentDescription = "Pass"
                                )
                            }
                            Column {
                                Text(
                                    text = if (state.isFounderPassPremiumUnlocked) "Founder Pass Pro" else "Founder Pass Free",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Season 1: Aesthetic Workspaces",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "Tier ${state.founderPassLevel}/20",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = if (state.isFounderPassPremiumUnlocked) Amber400 else Indigo300
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val passProgressRatio = state.founderPassXp.toFloat() / 100f
                    LinearProgressIndicator(
                        progress = passProgressRatio.coerceIn(0f, 1f),
                        color = if (state.isFounderPassPremiumUnlocked) Amber400 else Indigo500,
                        trackColor = White10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Progress is unlocked by advancing weeks (25 Pass XP per week) and claiming daily rewards.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    if (!state.isFounderPassPremiumUnlocked) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.purchaseFounderPassPro() },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Unlock Premium Rewards (500 Tokens / $4.99)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Tiers & Season Rewards",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // List of all 20 tiers
        items(ProgressionData.FOUNDER_PASS_TIERS) { tier ->
            val isUnlocked = state.founderPassLevel >= tier.tier
            val isPremium = state.isFounderPassPremiumUnlocked

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) White5 else White5.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isUnlocked) Indigo500.copy(alpha = 0.3f) else White10
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tier ${tier.tier}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = if (isUnlocked) Indigo300 else TextMuted
                        )

                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .background(Emerald400.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("UNLOCKED", fontSize = 8.sp, color = Emerald400, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Locked (Requires Level ${tier.tier})", fontSize = 8.sp, color = TextMuted)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Free track reward
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FREE TRACK",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(White10, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tier.freeRewardName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isUnlocked) {
                                    Button(
                                        onClick = { viewModel.claimFounderPassReward(tier.tier, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text("Claim", fontSize = 8.sp)
                                    }
                                }
                            }
                        }

                        // Premium track reward
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "PREMIUM TRACK",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber400
                                )
                                Icon(Icons.Default.Star, contentDescription = "Premium", tint = Amber400, modifier = Modifier.size(10.dp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isPremium) Amber400.copy(alpha = 0.05f) else White10.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isPremium) Amber400.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tier.premiumRewardName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) TextWhite else TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isUnlocked && isPremium) {
                                    Button(
                                        onClick = { viewModel.claimFounderPassReward(tier.tier, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text("Claim", fontSize = 8.sp, color = Color.Black)
                                    }
                                } else if (isUnlocked && !isPremium) {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(14.dp))
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
fun CustomizerSubTab(state: GamePrefs.GameState, viewModel: GameViewModel) {
    var activeCategory by remember { mutableStateOf("DESK") } // "DESK", "CHAIR", "PLANT", "WALL_ART"

    // Helper map to translate equipped IDs to nice display strings/emojis
    val deskDisplay = when (state.equippedDesk) {
        "desk_neon" -> "📟 Cyber Desk"
        "desk_executive" -> "🪵 Exec Mahogany"
        "desk_hologram" -> "🔮 Quantum Hologram"
        else -> "🗄️ Basic Desk"
    }
    val chairDisplay = when (state.equippedChair) {
        "chair_ergonomic" -> "💺 Mesh Ergonomic"
        "chair_gaming" -> "🎮 Rivals Gaming"
        "chair_gold" -> "👑 Golden Throne"
        else -> "🪑 Swivel Chair"
    }
    val plantDisplay = when (state.equippedPlant) {
        "plant_cactus" -> "🌵 Desk Cactus"
        "plant_bonsai" -> "🪴 Juniper Bonsai"
        "plant_bonsai_holo" -> "🌸 Hologram Sakura"
        else -> "🌿 Plastic Ficus"
    }
    val wallDisplay = when (state.equippedWallArt) {
        "wall_blueprint" -> "📜 Blueprint Canvas"
        "wall_neon_logo" -> "💡 Golden Neon Logo"
        "wall_nft" -> "🖼️ Pixel NFT Art"
        else -> "🐱 Motivation Cat"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Workspace Graphic Mockup Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, White10),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Wall item preview background centered
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Wall art: $wallDisplay", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = when (state.equippedWallArt) {
                            "wall_blueprint" -> "📝"
                            "wall_neon_logo" -> "⚡"
                            "wall_nft" -> "👾"
                            else -> "🐱"
                        },
                        fontSize = 28.sp
                    )
                }

                // Desk & Chair & Plant left/right foreground
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left plant
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (state.equippedPlant) {
                                "plant_cactus" -> "🌵"
                                "plant_bonsai" -> "🪴"
                                "plant_bonsai_holo" -> "🌸"
                                else -> "🌿"
                            },
                            fontSize = 24.sp
                        )
                        Text(plantDisplay.substringAfter(" "), fontSize = 8.sp, color = TextMuted)
                    }

                    // Desk center foreground
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = when (state.equippedChair) {
                                    "chair_ergonomic" -> "💺"
                                    "chair_gaming" -> "🏎️"
                                    "chair_gold" -> "👑"
                                    else -> "🪑"
                                },
                                fontSize = 24.sp
                            )
                            Text(
                                text = when (state.equippedDesk) {
                                    "desk_neon" -> "💻"
                                    "desk_executive" -> "💼"
                                    "desk_hologram" -> "⚛️"
                                    else -> "⌨️"
                                },
                                fontSize = 24.sp
                            )
                        }
                        Text("$deskDisplay • $chairDisplay", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category switches selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("DESK" to "Desks", "CHAIR" to "Chairs", "PLANT" to "Plants", "WALL_ART" to "Wall Art").forEach { (cat, label) ->
                val isSelected = activeCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Indigo500 else White5)
                        .border(1.dp, if (isSelected) Indigo500 else White10, RoundedCornerShape(8.dp))
                        .clickable { activeCategory = cat }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextWhite else TextMuted
                    )
                }
            }
        }

        // Scrollable list of items in category
        val categoryItems = ProgressionData.COSMETICS.filter { it.category == activeCategory }
        val unlockedSet = state.unlockedCosmetics.toSet()

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categoryItems) { item ->
                val isUnlocked = unlockedSet.contains(item.id)
                val isEquipped = when (activeCategory) {
                    "DESK" -> state.equippedDesk == item.id
                    "CHAIR" -> state.equippedChair == item.id
                    "PLANT" -> state.equippedPlant == item.id
                    "WALL_ART" -> state.equippedWallArt == item.id
                    else -> false
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White5, RoundedCornerShape(12.dp))
                        .border(1.dp, if (isEquipped) Indigo500 else White10, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(White10, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (item.id) {
                                    "desk_neon" -> "📟"
                                    "desk_executive" -> "🪵"
                                    "desk_hologram" -> "🔮"
                                    "chair_ergonomic" -> "💺"
                                    "chair_gaming" -> "🎮"
                                    "chair_gold" -> "👑"
                                    "plant_cactus" -> "🌵"
                                    "plant_bonsai" -> "🪴"
                                    "plant_bonsai_holo" -> "🌸"
                                    "wall_blueprint" -> "📜"
                                    "wall_neon_logo" -> "💡"
                                    "wall_nft" -> "🖼️"
                                    else -> "📦"
                                },
                                fontSize = 16.sp
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (item.rarity) {
                                                "LEGENDARY" -> Amber400.copy(alpha = 0.15f)
                                                "RARE" -> Indigo300.copy(alpha = 0.15f)
                                                else -> TextMuted.copy(alpha = 0.15f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.rarity,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (item.rarity) {
                                            "LEGENDARY" -> Amber400
                                            "RARE" -> Indigo300
                                            else -> TextMuted
                                        }
                                    )
                                }
                            }
                            Text(
                                text = if (item.isPremiumOnly) "Founder Pass Pro exclusive" else "Purchase with cosmetic tokens",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    if (isEquipped) {
                        Box(
                            modifier = Modifier
                                .background(Indigo500.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("EQUIPPED", fontSize = 9.sp, color = Indigo300, fontWeight = FontWeight.Bold)
                        }
                    } else if (isUnlocked) {
                        Button(
                            onClick = { viewModel.equipCosmetic(item.id, item.category) },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Equip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        if (item.isPremiumOnly) {
                            Icon(Icons.Default.Lock, contentDescription = "Premium lock", tint = Amber400, modifier = Modifier.size(16.dp))
                        } else {
                            Button(
                                onClick = { viewModel.buyCosmetic(item.id, item.tokenCost) },
                                enabled = state.cosmeticTokens >= item.tokenCost,
                                colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "${item.tokenCost} Tokens",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
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
fun VipSubTab(
    state: GamePrefs.GameState,
    viewModel: GameViewModel,
    onPurchaseClick: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Disclaimer Card: No Pay-To-Win
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Emerald400.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Emerald400.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        tint = Emerald400,
                        modifier = Modifier.size(24.dp),
                        contentDescription = "Safe Fair Play"
                    )
                    Column {
                        Text(
                            text = "Fair Play Guarantee • 100% Cosmetic Only",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "To preserve competitive integrity and keep global leaderboards balanced, in-game advantages are strictly earned through strategy, not wallets.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLight
                        )
                    }
                }
            }
        }

        // Subscriptions VIP section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, Amber400),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 18.sp)
                            Text(
                                text = "CEO Club Premium Membership",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                        }
                        Text(
                            text = "$4.99/mo",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = Amber400
                        )
                    }

                    Text(
                        text = "Become an elite member of the Silicon Valley Club. Instantly unlock all Premium cosmetic slots, remove all AdMob banner and interstitial ads, get a golden chat badge, and unlock the entire Premium tier of Founder Pass!",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (state.isSubscribedToCEOClub) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Emerald400.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, Emerald400, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ACTIVE CEO CLUB SUBSCRIPTION • Thank you for supporting fair play!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.subscribeToCEOClub()
                                onPurchaseClick("CEO Club Premium Subscription")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Subscribe via Google Play", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Direct Item Upgrades List
        item {
            Text(
                text = "Premium Company & Industry Expansions",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Premium Company Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(14.dp))
                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exclusive Industries expansion pack",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Unlocks unique atmospheric sectors like Quantum Computing and Space Mining! 🚀 Includes exclusive decorative foliage.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                if (state.isExclusiveIndustryUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(Emerald400.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("UNLOCKED", fontSize = 9.sp, color = Emerald400, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.unlockExclusiveIndustry()
                            onPurchaseClick("Exclusive Industries Pack")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("$2.99", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Token Shop (Simulated billing)
        item {
            Text(
                text = "Purchase Cosmetic Tokens (Includes Receipt Validation)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(14.dp))
                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🪙", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Standard Token Pack (+500 Tokens)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Buy instant cosmetic tokens for desk customization.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.gainXP(100L) // also rewards some baseline XP
                        // directly triggers validation workflow
                        onPurchaseClick("500 Cosmetic Tokens Pack")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$1.99", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White5, RoundedCornerShape(14.dp))
                    .border(1.dp, White10, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💰", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Whale Vault (+1500 Tokens)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Massive tokens boost. Includes 1 bonus Legendary chest!",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
                Button(
                    onClick = {
                        onPurchaseClick("1500 Cosmetic Tokens Vault")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$4.99", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}
