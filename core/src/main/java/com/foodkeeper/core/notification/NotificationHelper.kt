package com.foodkeeper.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

// Hilt 주입을 위해 @Inject constructor가 필요할 수 있습니다.
class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "food_keeper_channel"
        private const val CHANNEL_NAME = "식자재 알림"
        private const val GROUP_KEY = "com.foodkeeper.NOTIF_GROUP" // 그룹화를 위한 키
        private const val SUMMARY_ID = 0 // 요약 알림의 고유 ID
    }

    fun showNotification(title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // MainActivity를 실행하도록 설정 (알림 클릭 시)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "home")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 1. 개별 식자재 알림 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY) // ✅ 같은 그룹 키를 지정
            .build()

        // 2. 요약 알림(Summary) 생성
        // 알림이 2개 이상 쌓일 때 "식자재 알림 3개" 처럼 묶어주는 역할을 합니다.
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("식자재 유통기한 알림")
            .setContentText("확인하지 않은 알림이 있습니다.")
            .setStyle(NotificationCompat.InboxStyle() // 여러 줄로 보여주기 위한 스타일
                .setSummaryText("유통기한 마감 임박"))
            .setGroup(GROUP_KEY) // ✅ 개별 알림과 같은 그룹 키
            .setGroupSummary(true) // ✅ 이 알림이 요약본임을 명시
            .setAutoCancel(true)
            .build()

        // 각각의 알림은 고유 ID로 띄우고
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        // 요약 알림은 항상 같은 고유 ID(0)로 업데이트하여 하나로 묶이게 함
        notificationManager.notify(SUMMARY_ID, summaryNotification)    }
}
