package com.securecam.utils
import android.app.*; import android.content.Context; import android.os.Build; import android.os.VibrationEffect; import android.os.Vibrator; import android.os.VibratorManager
import androidx.core.app.NotificationCompat; import com.securecam.R; import com.securecam.SecureCamApp
object NotificationHelper {
    private var nid = 1000
    fun showMotionAlert(ctx: Context, score: Float) {
        val n = NotificationCompat.Builder(ctx, SecureCamApp.CHANNEL_MOTION)
            .setSmallIcon(R.drawable.ic_motion).setContentTitle("⚠️ Motion Detected!")
            .setContentText("Intensity: ${"%.0f".format(score*100)}%")
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
            .setVibrate(longArrayOf(0,300,100,300)).build()
        ctx.getSystemService(NotificationManager::class.java).notify(nid++, n)
    }
    fun showObjectDetectionAlert(ctx: Context, label: String, confidence: Float) {
        val n = NotificationCompat.Builder(ctx, SecureCamApp.CHANNEL_AI)
            .setSmallIcon(R.drawable.ic_ai).setContentTitle("🔍 Detected: $label")
            .setContentText("${"%.0f".format(confidence*100)}% confidence")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        ctx.getSystemService(NotificationManager::class.java).notify(nid++, n)
    }
    fun showFaceAlert(ctx: Context, count: Int) {
        val n = NotificationCompat.Builder(ctx, SecureCamApp.CHANNEL_AI)
            .setSmallIcon(R.drawable.ic_face).setContentTitle("👤 ${if(count==1)"Person" else "$count People"} Detected")
            .setContentText("Face detected in monitored area")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        ctx.getSystemService(NotificationManager::class.java).notify(nid++, n)
    }
    fun buildStreamingServiceNotification(ctx: Context): Notification =
        NotificationCompat.Builder(ctx, SecureCamApp.CHANNEL_STREAM)
            .setSmallIcon(R.drawable.ic_camera_stream).setContentTitle("SecureCam is streaming")
            .setContentText("Camera stream is live • AI monitoring active")
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).setSilent(true).build()
}