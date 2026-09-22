package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CoreComponent
import com.example.model.PlantStatus
import com.example.notification.TurbineWearNotificationManager
import com.example.ui.components.ComponentDetailModal
import com.example.ui.components.MaintenanceAlertBanner
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.AnalyticsMaintenanceScreen
import com.example.ui.screens.ControlsScreen
import com.example.ui.screens.DistributionScreen
import com.example.ui.screens.SchematicScreen
import com.example.ui.theme.HydroAlarmRed
import com.example.ui.theme.HydroCyanPrimary
import com.example.ui.theme.HydroNavyDark
import com.example.ui.theme.HydroNormalGreen
import com.example.ui.theme.HydroSurfaceDark
import com.example.ui.theme.HydroWarningAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.HydroTab
import com.example.viewmodel.HydroViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initialComponentExtra: String? = null
    private var openMaintenanceTabExtra: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractIntentExtras(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                HydroSimulatorApp(
                    initialComponent = initialComponentExtra,
                    openMaintenanceTab = openMaintenanceTabExtra
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractIntentExtras(intent)
    }

    private fun extractIntentExtras(intent: Intent?) {
        initialComponentExtra = intent?.getStringExtra("SELECT_COMPONENT")
        openMaintenanceTabExtra = intent?.getBooleanExtra("OPEN_MAINTENANCE_TAB", false) ?: false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydroSimulatorApp(
    viewModel: HydroViewModel = viewModel(),
    initialComponent: String? = null,
    openMaintenanceTab: Boolean = false
) {
    val context = LocalContext.current
    val plantState by viewModel.plantState.collectAsState()
    val selectedComponent by viewModel.selectedComponent.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val scenarios by viewModel.scenarios.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val activeBanners by viewModel.activeBanners.collectAsState()
    val isWearMonitorRunning by viewModel.isWearMonitorRunning.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Handle runtime notification permission for Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled gracefully
    }

    LaunchedEffect(Unit) {
        TurbineWearNotificationManager.createNotificationChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Route intent extras from notification click
        if (openMaintenanceTab) {
            viewModel.setActiveTab(HydroTab.MAINTENANCE)
        }
        if (initialComponent != null) {
            try {
                val comp = CoreComponent.valueOf(initialComponent)
                viewModel.selectComponent(comp)
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hydro_app_scaffold"),
        containerColor = HydroNavyDark,
        topBar = {
            HydroTopAppBar(
                activePowerMw = plantState.activePowerMw,
                frequencyHz = plantState.frequencyHz,
                plantStatus = plantState.status,
                isTripped = plantState.isEmergencyTrip,
                onEmergencyTrip = { viewModel.emergencyTrip() },
                onResetTrip = { viewModel.resetTrip() }
            )
        },
        bottomBar = {
            HydroBottomNavigationBar(
                activeTab = activeTab,
                onTabSelected = { viewModel.setActiveTab(it) },
                activeAlertCount = plantState.alerts.count { !it.resolved } + activeBanners.size
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Predictive Wear & Maintenance Local In-App Banner
            MaintenanceAlertBanner(
                alerts = activeBanners,
                onQuickService = { alert ->
                    viewModel.quickServiceAlert(alert, context)
                },
                onInspectComponent = { comp ->
                    viewModel.selectComponent(comp)
                },
                onDismissAlert = { alertId ->
                    viewModel.dismissBannerAlert(alertId)
                }
            )

            // Main tab screen content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    HydroTab.SCHEMATIC -> {
                        SchematicScreen(
                            plantState = plantState,
                            selectedComponent = selectedComponent,
                            onSelectComponent = { comp ->
                                viewModel.selectComponent(comp)
                            },
                            onEmergencyTrip = { viewModel.emergencyTrip() }
                        )
                    }
                    HydroTab.CONTROLS -> {
                        ControlsScreen(
                            plantState = plantState,
                            onSetMode = { viewModel.setOperationalMode(it) },
                            onUpdateIntakeGate = { viewModel.updateIntakeGate(it) },
                            onUpdateMivOpening = { viewModel.updateMivOpening(it) },
                            onToggleBypassValve = { viewModel.toggleBypassValve() },
                            onUpdateGuideVanes = { viewModel.updateGuideVanes(it) },
                            onUpdateExciterCurrent = { viewModel.updateExciterCurrent(it) },
                            onToggleSynchronize = { viewModel.toggleSynchronize() },
                            onEmergencyTrip = { viewModel.emergencyTrip() },
                            onResetTrip = { viewModel.resetTrip() }
                        )
                    }
                    HydroTab.DISTRIBUTION -> {
                        DistributionScreen(
                            plantState = plantState,
                            onToggleFeeder = { viewModel.toggleFeeder(it) },
                            onUpdateFeederAllocation = { id, mw -> viewModel.updateFeederAllocation(id, mw) }
                        )
                    }
                    HydroTab.MAINTENANCE -> {
                        AnalyticsMaintenanceScreen(
                            plantState = plantState,
                            isMonitoringServiceRunning = isWearMonitorRunning,
                            onToggleMonitoringService = { viewModel.toggleBackgroundMonitoring(context) },
                            onTriggerSimulatedWear = { comp -> viewModel.triggerSimulatedWear(comp, context) },
                            onPerformMaintenanceAction = { viewModel.performMaintenanceAction(it) },
                            onResolveAlert = { viewModel.resolveAlert(it) }
                        )
                    }
                    HydroTab.AI_ADVISOR -> {
                        AiAdvisorScreen(
                            plantState = plantState,
                            recommendations = recommendations,
                            chatMessages = chatMessages,
                            scenarios = scenarios,
                            isAiLoading = isAiLoading,
                            onRefreshAi = { viewModel.refreshAiAnalysis() },
                            onLoadScenario = { viewModel.loadScenario(it) },
                            onAskAi = { viewModel.askAi(it) }
                        )
                    }
                }

                // Component Detail Inspector Modal BottomSheet
                if (selectedComponent != null) {
                    ComponentDetailModal(
                        component = selectedComponent,
                        plantState = plantState,
                        sheetState = sheetState,
                        onDismiss = {
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                viewModel.selectComponent(null)
                            }
                        },
                        onPerformMaintenance = { comp ->
                            viewModel.performMaintenance(comp)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HydroTopAppBar(
    activePowerMw: Float,
    frequencyHz: Float,
    plantStatus: PlantStatus,
    isTripped: Boolean,
    onEmergencyTrip: () -> Unit,
    onResetTrip: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(
                                color = when (plantStatus) {
                                    PlantStatus.NORMAL -> HydroNormalGreen
                                    PlantStatus.WARNING -> HydroWarningAmber
                                    PlantStatus.TRIPPED -> HydroAlarmRed
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SURYA ENERGY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "HYDRO STATION • ${String.format("%.1f", activePowerMw)} MW • ${String.format("%.2f", frequencyHz)} Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = HydroCyanPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            if (isTripped) {
                Button(
                    onClick = onResetTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = HydroNormalGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("appbar_reset_trip_btn")
                ) {
                    Text("Reset", color = HydroNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = Color(0xFF261014),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HydroAlarmRed.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "ONLINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroNormalGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = HydroSurfaceDark
        )
    )
}

@Composable
private fun HydroBottomNavigationBar(
    activeTab: HydroTab,
    onTabSelected: (HydroTab) -> Unit,
    activeAlertCount: Int
) {
    NavigationBar(
        containerColor = HydroSurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        HydroTab.values().forEach { tab ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == HydroTab.MAINTENANCE && activeAlertCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = HydroAlarmRed,
                                    contentColor = Color.White
                                ) {
                                    Text("$activeAlertCount")
                                }
                            }
                        ) {
                            TabIcon(tab = tab, isSelected = isSelected)
                        }
                    } else {
                        TabIcon(tab = tab, isSelected = isSelected)
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HydroNavyDark,
                    selectedTextColor = HydroCyanPrimary,
                    indicatorColor = HydroCyanPrimary,
                    unselectedIconColor = TextSecondaryDark,
                    unselectedTextColor = TextSecondaryDark
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name}")
            )
        }
    }
}

@Composable
private fun TabIcon(tab: HydroTab, isSelected: Boolean) {
    Icon(
        imageVector = when (tab) {
            HydroTab.SCHEMATIC -> Icons.Default.Dashboard
            HydroTab.CONTROLS -> Icons.Default.Tune
            HydroTab.DISTRIBUTION -> Icons.Default.ElectricBolt
            HydroTab.MAINTENANCE -> Icons.Default.Engineering
            HydroTab.AI_ADVISOR -> Icons.Default.AutoAwesome
        },
        contentDescription = tab.label,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
