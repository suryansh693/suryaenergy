package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.model.CoreComponent
import com.example.model.MaintenanceBannerAlert
import com.example.model.WearSeverity
import com.example.notification.TurbineWearNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TurbineWearMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var lastNotifiedTime = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        TurbineWearNotificationManager.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                return START_NOT_STICKY
            }
            ACTION_TRIGGER_SIMULATED_ALERT -> {
                val componentName = intent.getStringExtra(EXTRA_COMPONENT) ?: CoreComponent.TURBINE.name
                val component = try {
                    CoreComponent.valueOf(componentName)
                } catch (_: Exception) {
                    CoreComponent.TURBINE
                }
                triggerImmediateWearAlert(component)
            }
            else -> {
                startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        TurbineWearMonitorBridge.setServiceRunning(true)

        val notification = TurbineWearNotificationManager.buildForegroundServiceNotification(
            this,
            "Monitoring runner cavitation, bearing oil film & thermal limits"
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    TurbineWearNotificationManager.FOREGROUND_SERVICE_NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(
                    TurbineWearNotificationManager.FOREGROUND_SERVICE_NOTIFICATION_ID,
                    notification
                )
            }
        } catch (_: Exception) {
            // Gracefully handle if foreground service start encounters policy/permission restriction
        }

        serviceScope.launch {
            while (isActive) {
                delay(3000)
                // Routine telemetry check loop
            }
        }
    }

    private fun triggerImmediateWearAlert(component: CoreComponent) {
        val now = System.currentTimeMillis()
        val alert = when (component) {
            CoreComponent.TURBINE -> {
                MaintenanceBannerAlert(
                    id = "alert_cavitation_${now}",
                    component = CoreComponent.TURBINE,
                    severity = WearSeverity.CRITICAL,
                    title = "Critical Runner Cavitation Detected",
                    description = "Francis runner blade pitting detected (Thoma Sigma σ < 0.082). Urgent runner blade grinding and laser cladding needed.",
                    currentHealthPercent = 46.5f,
                    actionType = "CAVITATION",
                    actionLabel = "Hone Runner Blades"
                )
            }
            CoreComponent.SHAFT -> {
                MaintenanceBannerAlert(
                    id = "alert_bearing_${now}",
                    component = CoreComponent.SHAFT,
                    severity = WearSeverity.WARNING,
                    title = "Thrust Bearing Lubrication Degradation",
                    description = "Shaft vibration spiked to 4.2 mm/s with bearing temp at 72.4°C. Oil viscosity breakdown in guide pads.",
                    currentHealthPercent = 52.0f,
                    actionType = "LUBRICATION",
                    actionLabel = "Flush Bearing Lube"
                )
            }
            CoreComponent.MAIN_INLET_VALVE -> {
                MaintenanceBannerAlert(
                    id = "alert_miv_${now}",
                    component = CoreComponent.MAIN_INLET_VALVE,
                    severity = WearSeverity.WARNING,
                    title = "MIV Spherical Seal Leakage",
                    description = "High differential pressure leakage detected across MIV spherical disc. Service seal seals.",
                    currentHealthPercent = 64.0f,
                    actionType = "MIV",
                    actionLabel = "Replace MIV Seals"
                )
            }
            CoreComponent.TRANSFORMER -> {
                MaintenanceBannerAlert(
                    id = "alert_transformer_${now}",
                    component = CoreComponent.TRANSFORMER,
                    severity = WearSeverity.WARNING,
                    title = "GSU Transformer Dielectric Breakdown",
                    description = "Dielectric oil breakdown voltage dropped below 30 kV with combustible dissolved gas accumulation.",
                    currentHealthPercent = 58.0f,
                    actionType = "TRANSFORMER",
                    actionLabel = "Degas Transformer Oil"
                )
            }
            else -> {
                MaintenanceBannerAlert(
                    id = "alert_generic_${now}",
                    component = component,
                    severity = WearSeverity.WARNING,
                    title = "${component.displayName} Scheduled Maintenance",
                    description = "Asset wear telemetry exceeds nominal operating envelopes. Preventive service requested.",
                    currentHealthPercent = 59.0f,
                    actionType = "LUBRICATION",
                    actionLabel = "Service Asset"
                )
            }
        }

        TurbineWearMonitorBridge.postAlert(alert)

        // Trigger Android local push notification
        TurbineWearNotificationManager.triggerWearNotification(
            context = this,
            notificationId = when (component) {
                CoreComponent.TURBINE -> 1001
                CoreComponent.SHAFT -> 1002
                CoreComponent.MAIN_INLET_VALVE -> 1003
                CoreComponent.TRANSFORMER -> 1004
                else -> 1005
            },
            title = alert.title,
            message = alert.description,
            component = alert.component,
            severity = alert.severity
        )
    }

    private fun stopMonitoring() {
        TurbineWearMonitorBridge.setServiceRunning(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        TurbineWearMonitorBridge.setServiceRunning(false)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.service.action.START_MONITOR"
        const val ACTION_STOP = "com.example.service.action.STOP_MONITOR"
        const val ACTION_TRIGGER_SIMULATED_ALERT = "com.example.service.action.TRIGGER_SIMULATED_ALERT"
        const val EXTRA_COMPONENT = "extra_component"

        fun start(context: Context) {
            val intent = Intent(context, TurbineWearMonitorService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
                // In background restrictions, fallback to startService
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TurbineWearMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun triggerSimulatedWearAlert(context: Context, component: CoreComponent) {
            val intent = Intent(context, TurbineWearMonitorService::class.java).apply {
                action = ACTION_TRIGGER_SIMULATED_ALERT
                putExtra(EXTRA_COMPONENT, component.name)
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {
                // If service cannot be started directly, post directly via bridge
                val now = System.currentTimeMillis()
                TurbineWearMonitorBridge.postAlert(
                    MaintenanceBannerAlert(
                        id = "alert_manual_${now}",
                        component = component,
                        severity = WearSeverity.CRITICAL,
                        title = "Cavitation Alert: ${component.displayName}",
                        description = "Immediate preventive maintenance required for ${component.displayName} due to simulated severe wear telemetry.",
                        currentHealthPercent = 48.0f,
                        actionType = "CAVITATION",
                        actionLabel = "Perform Overhaul"
                    )
                )
                TurbineWearNotificationManager.triggerWearNotification(
                    context = context,
                    notificationId = 1001,
                    title = "Cavitation Alert: ${component.displayName}",
                    message = "Immediate preventive maintenance required for ${component.displayName}.",
                    component = component,
                    severity = WearSeverity.CRITICAL
                )
            }
        }
    }
}
