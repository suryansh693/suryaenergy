package com.example.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.CoreComponent
import com.example.model.WearSeverity

object TurbineWearNotificationManager {

    const val CHANNEL_ID = "turbine_wear_alerts_channel"
    const val FOREGROUND_CHANNEL_ID = "turbine_wear_monitor_channel"
    const val FOREGROUND_SERVICE_NOTIFICATION_ID = 9001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // 1. Wear & Maintenance Alert Channel (High Importance, heads-up)
            val alertChannel = NotificationChannel(
                CHANNEL_ID,
                "Turbine Wear & Maintenance Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggered when turbine runner, bearings, MIV, or transformer reach critical wear thresholds"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(alertChannel)

            // 2. Foreground Monitoring Service Channel (Low/Default Importance)
            val monitorChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Hydro Telemetry Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service monitoring real-time turbine vibration, cavitation, and thermal wear"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(monitorChannel)
        }
    }

    fun buildForegroundServiceNotification(context: Context, statusMessage: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Turbine Wear Telemetry Active")
            .setContentText(statusMessage)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun triggerWearNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        component: CoreComponent,
        severity: WearSeverity
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SELECT_COMPONENT", component.name)
            putExtra("OPEN_MAINTENANCE_TAB", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(
                if (severity == WearSeverity.CRITICAL) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(if (severity == WearSeverity.CRITICAL) 0xFFE12D39.toInt() else 0xFFF0B429.toInt())

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Handled gracefully if POST_NOTIFICATIONS runtime permission has not been granted yet
        } catch (_: Exception) {
            // Catch any unexpected notification failure
        }
    }

    fun dismissNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
        } catch (_: Exception) {
            // Ignore
        }
    }
}
