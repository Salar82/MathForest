package ir.alishojaee.mathforest.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ir.alishojaee.mathforest.R
import ir.alishojaee.mathforest.utils.getInstallerPackageName


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]

        if (title.isNullOrEmpty())
            return

        val isAppInstallNotif = data["is_app_install"]?.toBoolean() == true
        val targetPackage = data["package_name"]

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "notif_push"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MathForest",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.cat)
            .setAutoCancel(true)

        if (isAppInstallNotif && !targetPackage.isNullOrEmpty()) {
            val installer = getInstallerPackageName(this, packageName)
            val marketUrl = when (installer) {
                "com.farsitel.bazaar" -> "https://cafebazaar.ir/app/$targetPackage"
                "ir.mservices.market" -> "https://myket.ir/app/$targetPackage"
                else -> "https://cafebazaar.ir/app/$targetPackage"
            }

            try {
                val intent = Intent(Intent.ACTION_VIEW, marketUrl.toUri())
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.setContentIntent(pendingIntent)
            } catch (_: Exception) {
            }
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}