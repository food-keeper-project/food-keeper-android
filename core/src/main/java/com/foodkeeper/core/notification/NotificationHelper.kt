package com.foodkeeper.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
// R 파일 임포트 확인 (core 모듈의 R)
import com.foodkeeper.core.R

// Hilt 주입을 위해 @Inject constructor가 필요할 수 있습니다.
class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "food_keeper_channel"
        private const val CHANNEL_NAME = "식자재 알림"
        private const val GROUP_KEY = "com.com.swyp.com.swyp.com.swyp.com.swyp.foodkeeper.NOTIF_GROUP" // 그룹화를 위한 키
        private const val SUMMARY_ID = 0 // 요약 알림의 고유 ID
    }

    // ... 상단 import 생략

    fun showNotification(title: String, body: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. 알림 채널 설정 (Android 8.0 이상 대응)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "식자재 유통기한 및 서비스 알림을 제공합니다."
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableLights(true)
                enableVibration(true)
                // ✅ 잠금화면에서 알림이 올 때 화면을 깨우도록 설정 (일부 기기)
                setShowBadge(true)
                setBypassDnd(true) // 방해금지 모드 우회 (선택 사항)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ 2. PendingIntent 설정 (빨간 줄 해결)
        // 앱의 런처 인텐트를 가져와서 메인 화면으로 연결합니다.
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // 필요 시 특정 화면 이동을 위한 데이터 전달 가능
            putExtra("navigate_to", "main")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            // Android 12 이상 대응을 위해 IMMUTABLE 또는 MUTABLE 플래그 필수
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. 개별 식자재 알림 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // ✅ 이제 에러가 발생하지 않습니다.
            .setGroup(GROUP_KEY)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // ✅ 수정한 부분: HIGH -> MAX (최고 수준의 우선순위)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // ✅ 추가: 시스템이 알림의 성격을 파악해 즉시 노출하도록 카테고리 설정
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 또는 CATEGORY_MESSAGE
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // 4. 요약 알림(Summary) 생성
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("식자재 유통기한 알림")
            .setContentText("확인하지 않은 알림이 있습니다.")
            .setStyle(NotificationCompat.InboxStyle().setSummaryText("유통기한 마감 임박"))
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // 요약 알림 클릭 시에도 이동하도록 추가
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        notificationManager.notify(SUMMARY_ID, summaryNotification)
    }
}
